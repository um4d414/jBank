package ru.umd.jbank.front_ui.integration.client.account.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record AccountDto(
    Long id,
    String username,
    String password,
    String firstname,
    String lastname,
    String email,
    LocalDate birthdate,
    List<BankAccountDto> bankingAccounts
) {
}
