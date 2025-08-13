package ru.umd.jbank.account.service.exception;

import java.util.Currency;

public class BankAccountAlreadyExistsException extends RuntimeException {
    public BankAccountAlreadyExistsException(Long accountId, Currency currency) {
        super("Банковский счет в валюте " + currency.getCurrencyCode() + 
              " уже существует для аккаунта с ID " + accountId);
    }
}
