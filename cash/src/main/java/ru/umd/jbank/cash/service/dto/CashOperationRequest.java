package ru.umd.jbank.cash.service.dto;

import java.math.BigDecimal;

public record CashOperationRequest(
    Long accountId,
    BigDecimal amount,
    Type operationType
) {
    public enum Type {
        DEPOSIT,
        WITHDRAWAL
    }
}
