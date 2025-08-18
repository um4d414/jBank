package ru.umd.jbank.front_ui.integration.client.account.dto;

import java.util.Currency;

public record CreateBankAccountRequestDto(
    Currency currency
) {
}
