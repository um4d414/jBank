package ru.umd.jbank.front_ui.integration.client.account.dto;

import java.time.LocalDate;

public record CreateAccountRequestDto(
    String lastName,
    String firstName,
    String email,
    LocalDate birthDate,
    String username,
    String password
) {}
