package ru.umd.jbank.account.data.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class AccountDto {
    private Long id;

    private String username;

    private String password;

    private String firstname;

    private String lastname;

    private String email;

    private LocalDate birthdate;

    private List<BankAccountDto> bankingAccounts;
}
