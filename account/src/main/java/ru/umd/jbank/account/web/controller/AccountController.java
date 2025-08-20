package ru.umd.jbank.account.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.account.data.dto.AccountDto;
import ru.umd.jbank.account.service.AccountManager;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private final AccountManager accountManager;

    @GetMapping("/account/{id}")
    public AccountDto getAccount(@PathVariable Long id, Authentication authentication) {
        logAuthenticationInfo(authentication, "getAccount");
        return accountManager.findAccount(id);
    }

    @PostMapping("/account")
    public AccountDto createAccount(@RequestBody AccountManager.CreateAccountRequestDto requestDto, Authentication authentication) {
        logAuthenticationInfo(authentication, "createAccount");
        return accountManager.createAccount(requestDto);
    }

    @GetMapping("/account/by-username/{username}")
    public AccountDto getAccountByUsername(@PathVariable String username, Authentication authentication) {
        logAuthenticationInfo(authentication, "getAccountByUsername");
        return accountManager.findAccountByUsername(username);
    }

    @GetMapping("/account/bank-account/{bankAccountId}/owner")
    public Long getBankAccountOwnerId(@PathVariable Long bankAccountId, Authentication authentication) {
        logAuthenticationInfo(authentication, "getBankAccountOwnerId");
        return accountManager.getBankAccountOwnerId(bankAccountId);
    }

    private void logAuthenticationInfo(Authentication authentication, String method) {
        if (authentication == null) {
            log.warn("Метод {}: Аутентификация отсутствует!", method);
            return;
        }

        log.info("Метод {}: Аутентификация получена - тип: {}, principal: {}",
                 method, authentication.getClass().getSimpleName(), authentication.getName());

        if (authentication.getPrincipal() instanceof Jwt jwt) {
            log.info("JWT токен - subject: {}, clientId: {}",
                     jwt.getSubject(), jwt.getClaim("azp"));
        }
    }
}