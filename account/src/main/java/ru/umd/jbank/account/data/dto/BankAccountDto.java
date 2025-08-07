package ru.umd.jbank.account.data.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Currency;

@Data
@Builder
public class BankAccountDto {
    private Long id;

    private Currency currency;

    private BigDecimal amount;
}
