package ru.umd.jbank.front_ui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.umd.jbank.front_ui.integration.client.account.AccountClient;
import ru.umd.jbank.front_ui.integration.client.account.dto.BankAccountDto;
import ru.umd.jbank.front_ui.integration.client.account.dto.CreateBankAccountRequestDto;

import java.util.Currency;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankAccountService {
    private final AccountClient accountClient;
    
    public BankAccountDto createBankAccount(Long accountId, Currency currency) {
        log.info("Создание банковского счета для аккаунта {} в валюте {}", accountId, currency);
        
        var request = new CreateBankAccountRequestDto(currency);
        var createdAccount = accountClient.createBankAccount(accountId, request);
        
        log.info("Создан банковский счет с ID {} для аккаунта {}", 
                 createdAccount.id(), accountId);
        
        return createdAccount;
    }
}
