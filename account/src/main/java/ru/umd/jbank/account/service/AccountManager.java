package ru.umd.jbank.account.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.umd.jbank.account.data.dto.AccountDto;
import ru.umd.jbank.account.data.dto.BankAccountDto;
import ru.umd.jbank.account.data.repository.AccountRepository;
import ru.umd.jbank.account.data.repository.BankingAccountRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountManager {
    private final AccountRepository accountRepository;

    private final BankingAccountRepository bankingAccountRepository;

    public Optional<AccountDto> findAccount(Long id) {
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
            );
    }
}
