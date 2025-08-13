package ru.umd.jbank.account.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.account.data.dto.AccountDto;
import ru.umd.jbank.account.service.AccountManager;


@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountManager accountManager;

    @GetMapping("/account/{id}")
    public AccountDto getAccount(@PathVariable Long id) {
        return accountManager.findAccount(id);
    }

    @PostMapping("/account")
    public AccountDto createAccount(@RequestBody AccountManager.CreateAccountRequestDto requestDto) {
        return accountManager.createAccount(requestDto);
    }
}
