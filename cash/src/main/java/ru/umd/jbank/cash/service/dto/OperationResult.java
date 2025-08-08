package ru.umd.jbank.cash.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OperationResult(
    boolean success,
    String message,
    BigDecimal newBalance,
    LocalDateTime operationTime
) {
}
