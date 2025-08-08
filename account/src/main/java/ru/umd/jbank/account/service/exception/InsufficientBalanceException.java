package ru.umd.jbank.account.service.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends BalanceServiceError {
    private final Long accountId;

    private final BigDecimal currentBalance;

    private final BigDecimal requestedAmount;

    public InsufficientBalanceException(Long accountId, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super(String.format(
            "Недостаточно средств на счете %d. Текущий баланс: %s, запрошенная сумма: %s",
            accountId, currentBalance, requestedAmount
        ));
        this.accountId = accountId;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }

    public Long getAccountId() {
        return accountId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }
}
