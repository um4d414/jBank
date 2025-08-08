package ru.umd.jbank.account.service.exception;

public class AccountNotFoundException extends BalanceServiceError {
    private final Long accountId;

    public AccountNotFoundException(Long accountId) {
        super(String.format("Банковский счет с ID %d не найден", accountId));
        this.accountId = accountId;
    }

    public Long getAccountId() {
        return accountId;
    }
}
