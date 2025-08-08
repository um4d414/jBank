package ru.umd.jbank.notification.integration.client.account.dto;

import java.time.LocalDate;
import java.util.List;

public record AccountDto(
    Long id,
    String firstname,
    String lastname,
    String email,
    LocalDate birthdate,
    List<BankAccountDto> bankingAccounts
) {
}
