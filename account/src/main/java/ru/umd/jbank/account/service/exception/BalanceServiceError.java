package ru.umd.jbank.account.service.exception;

public class BalanceServiceError extends RuntimeException {
    public BalanceServiceError(String message) {
        super(message);
    }
}
