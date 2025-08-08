package ru.umd.jbank.account.service.exception;

import java.math.BigDecimal;

public class InvalidAmountException extends BalanceServiceError {
    private final BigDecimal amount;

    public InvalidAmountException(BigDecimal amount) {
        super(String.format("Некорректная сумма операции: %s", amount));
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
