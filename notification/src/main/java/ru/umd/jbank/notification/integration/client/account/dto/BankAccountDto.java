package ru.umd.jbank.notification.integration.client.account.dto;

import java.math.BigDecimal;
import java.util.Currency;

public record BankAccountDto(
    Long id,
    Currency currency,
    BigDecimal amount
) {
}
