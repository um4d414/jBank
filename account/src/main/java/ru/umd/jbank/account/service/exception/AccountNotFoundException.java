package ru.umd.jbank.account.service.exception;

public class AccountNotFoundException extends BalanceServiceError {
    public AccountNotFoundException(Long accountId) {
        super(String.format("УЗ с ID %d не найдена", accountId));
    }

    public AccountNotFoundException(String login) {
        super(String.format("УЗ с login %s не найдена", login));
    }
}