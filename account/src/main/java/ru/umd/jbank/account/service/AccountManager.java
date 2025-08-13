package ru.umd.jbank.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.umd.jbank.account.data.dto.AccountDto;
import ru.umd.jbank.account.data.dto.BankAccountDto;
import ru.umd.jbank.account.data.entity.AccountEntity;
import ru.umd.jbank.account.data.entity.BankAccountEntity;
import ru.umd.jbank.account.data.repository.AccountRepository;
import ru.umd.jbank.account.data.repository.BankingAccountRepository;
import ru.umd.jbank.account.service.exception.*;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    public BankAccountDto createBankAccount(CreateBankAccountRequestDto requestDto) {
        var account = accountRepository
            .findById(requestDto.accountId())
            .orElseThrow(() -> new AccountNotFoundException(requestDto.accountId()));

        boolean currencyExists = bankingAccountRepository
            .existsByAccountIdAndCurrency(requestDto.accountId(), requestDto.currency());

        if (currencyExists) {
            throw new BankAccountAlreadyExistsException(requestDto.accountId(), requestDto.currency());
        }

        var bankAccountEntity = BankAccountEntity
            .builder()
            .currency(requestDto.currency())
            .amount(BigDecimal.ZERO)
            .account(account)
            .build();

        var savedBankAccount = bankingAccountRepository.save(bankAccountEntity);

        return BankAccountDto.builder()
            .id(savedBankAccount.getId())
            .currency(savedBankAccount.getCurrency())
            .amount(savedBankAccount.getAmount())
            .build();
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

    public AccountDto createAccount(CreateAccountRequestDto requestDto) {
        if (accountRepository.existsByLogin(requestDto.username())) {
            throw new UserAlreadyExistsException(requestDto.username());
        }

        if (requestDto.birthDate().isAfter(LocalDate.now().minusYears(18))) {
            throw new InvalidAgeException("Возраст должен быть не менее 18 лет");
        }

        var accountEntity = AccountEntity.builder()
            .login(requestDto.username())
            .password(requestDto.password()) // В реальном приложении здесь должно быть шифрование
            .firstname(requestDto.firstName())
            .lastname(requestDto.lastName())
            .email(requestDto.email())
            .birthdate(requestDto.birthDate())
            .build();

        var savedAccount = accountRepository.save(accountEntity);

        return AccountDto.builder()
            .id(savedAccount.getId())
            .firstname(savedAccount.getFirstname())
            .lastname(savedAccount.getLastname())
            .birthdate(savedAccount.getBirthdate())
            .email(savedAccount.getEmail())
            .bankingAccounts(java.util.Collections.emptyList())
            .build();

    }

    public record CreateBankAccountRequestDto(
        Long accountId,
        java.util.Currency currency
    ) {
    }

    public record CashOperation(Long accountId, Type type, BigDecimal amount) {
        public enum Type {
            DEPOSIT,
            WITHDRAWAL
        }
    }

    public record CreateAccountRequestDto(
        String lastName,
        String firstName,
        String email,
        LocalDate birthDate,
        String username,
        String password
    ) {
    }
}
