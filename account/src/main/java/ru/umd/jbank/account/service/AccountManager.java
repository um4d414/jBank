package ru.umd.jbank.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.umd.jbank.account.data.dto.AccountDto;
import ru.umd.jbank.account.data.dto.BankAccountDto;
import ru.umd.jbank.account.data.repository.AccountRepository;
import ru.umd.jbank.account.data.repository.BankingAccountRepository;
import ru.umd.jbank.account.service.exception.*;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountManager {
    private final AccountRepository accountRepository;

    private final BankingAccountRepository bankingAccountRepository;

    public AccountDto findAccount(Long id) {
        var bankingAccounts = bankingAccountRepository
            .findAllByAccountId(id)
            .stream()
            .map(
                it -> BankAccountDto
                    .builder()
                    .id(it.getId())
                    .amount(it.getAmount())
                    .currency(it.getCurrency())
                    .build()
            )
            .toList();

        return accountRepository
            .findById(id)
            .map(
                it -> AccountDto.builder()
                    .id(it.getId())
                    .firstname(it.getFirstname())
                    .lastname(it.getLastname())
                    .birthdate(it.getBirthdate())
                    .email(it.getEmail())
                    .bankingAccounts(bankingAccounts)
                    .build()
            )
            .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public void makeCashOperation(CashOperation cashOperation) {
        if (
            cashOperation.amount() == null ||
                cashOperation.amount().compareTo(BigDecimal.ZERO) <= 0
        ) {
            throw new InvalidAmountException(cashOperation.amount());
        }

        var bankingAccount = bankingAccountRepository
            .findById(cashOperation.accountId())
            .orElseThrow(() -> new AccountNotFoundException(cashOperation.accountId()));

        switch (cashOperation.type) {
            case DEPOSIT -> {
                bankingAccount.setAmount(bankingAccount.getAmount().add(cashOperation.amount()));
                bankingAccountRepository.save(bankingAccount);
            }
            case WITHDRAWAL -> {
                if (bankingAccount.getAmount().compareTo(cashOperation.amount()) < 0) {
                    throw new InsufficientBalanceException(
                        cashOperation.accountId(),
                        bankingAccount.getAmount(),
                        cashOperation.amount()
                    );
                }

                bankingAccount.setAmount(bankingAccount.getAmount().subtract(cashOperation.amount()));
                bankingAccountRepository.save(bankingAccount);
            }
        }
    }

    public record CashOperation(Long accountId, Type type, BigDecimal amount) {
        public enum Type {
            DEPOSIT,
            WITHDRAWAL
        }
    }
}
