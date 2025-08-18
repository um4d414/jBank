package ru.umd.jbank.front_ui.web.dto;

import java.math.BigDecimal;

public record ExchangeRateDto(
    String currency,
    BigDecimal rate
) {
    public static ExchangeRateDto fromRate(String currency, BigDecimal rate) {
        return new ExchangeRateDto(currency, rate);
    }
}
