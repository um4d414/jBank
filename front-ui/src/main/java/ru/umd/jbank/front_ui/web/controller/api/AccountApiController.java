package ru.umd.jbank.front_ui.web.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.front_ui.service.AccountService;
import ru.umd.jbank.front_ui.service.BankAccountService;

import java.util.Currency;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AccountApiController {
    
    private final AccountService accountService;
    private final BankAccountService bankAccountService;

    @GetMapping("/accounts/search-receiver/{username}")
    public ResponseEntity<?> searchReceiver(@PathVariable String username, Authentication authentication) {
        try {
            String currentUser = authentication.getName();
            log.info("Поиск получателя '{}' пользователем '{}'", username, currentUser);

            // Не позволяем искать самого себя
            if (username.equals(currentUser)) {
                return ResponseEntity.badRequest().body("Нельзя переводить самому себе");
            }

            // Получаем пользователя через AccountClient
            var receiverAccount = accountService.getAccountByUsername(username);
            if (receiverAccount == null) {
                return ResponseEntity.notFound().build();
            }

            // Банковские счета получателя уже есть в AccountDto
            var receiverBankAccounts = receiverAccount.bankingAccounts();

            if (receiverBankAccounts == null || receiverBankAccounts.isEmpty()) {
                log.info("У пользователя '{}' нет банковских счетов", username);
                return ResponseEntity.ok(java.util.Collections.emptyList());
            }

            log.info("Найдено {} счетов для получателя '{}'", receiverBankAccounts.size(), username);
            return ResponseEntity.ok(receiverBankAccounts);

        } catch (Exception e) {
            log.error("Ошибка при поиске получателя: {}", username, e);
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/accounts/{accountId}/bank-accounts")
    public ResponseEntity<?> createBankAccount(
            @PathVariable Long accountId,
            @RequestParam String currency,
            Authentication authentication) {
        
        try {
            String username = authentication.getName();
            log.info("API: Создание банковского счета в валюте {} для пользователя {}", currency, username);
            
            // Проверяем, что пользователь создает счет для себя
            var currentUser = accountService.getAccountByUsername(username);
            if (!currentUser.id().equals(accountId)) {
                return ResponseEntity.badRequest().body("Нельзя создавать счета для других пользователей");
            }
            
            var currencyInstance = Currency.getInstance(currency);
            var createdAccount = bankAccountService.createBankAccount(accountId, currencyInstance);
            
            log.info("Банковский счет успешно создан: {}", createdAccount);
            return ResponseEntity.ok(createdAccount);
            
        } catch (IllegalArgumentException e) {
            log.error("Неверная валюта: {}", currency, e);
            return ResponseEntity.badRequest().body("Неверная валюта: " + currency);
            
        } catch (Exception e) {
            log.error("Ошибка при создании банковского счета через API", e);
            
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("already exists")) {
                return ResponseEntity.badRequest().body("У вас уже есть счет в валюте " + currency);
            } else {
                return ResponseEntity.internalServerError().body("Ошибка при создании банковского счета: " + errorMessage);
            }
        }
    }

    @DeleteMapping("/accounts/{accountId}/bank-accounts/{bankAccountId}")
    public ResponseEntity<?> deleteBankAccount(
            @PathVariable Long accountId,
            @PathVariable Long bankAccountId,
            Authentication authentication) {
        
        try {
            String username = authentication.getName();
            log.info("API: Удаление банковского счета {} для пользователя {}", bankAccountId, username);
            
            // Проверяем, что пользователь удаляет свой счет
            var currentUser = accountService.getAccountByUsername(username);
            if (!currentUser.id().equals(accountId)) {
                return ResponseEntity.badRequest().body("Нельзя удалять счета других пользователей");
            }
            
            // TODO: Реализовать удаление банковского счета через BankAccountService
            // bankAccountService.deleteBankAccount(bankAccountId, accountId);
            
            log.info("Банковский счет {} успешно удален", bankAccountId);
            return ResponseEntity.ok().body("Банковский счет удален");
            
        } catch (Exception e) {
            log.error("Ошибка при удалении банковского счета через API", e);
            return ResponseEntity.internalServerError().body("Ошибка при удалении банковского счета: " + e.getMessage());
        }
    }

    @GetMapping("/accounts/current")
    public ResponseEntity<?> getCurrentAccount(Authentication authentication) {
        try {
            String username = authentication.getName();
            var currentUser = accountService.getAccountByUsername(username);
            return ResponseEntity.ok(currentUser);
        } catch (Exception e) {
            log.error("Ошибка при получении текущего пользователя", e);
            return ResponseEntity.internalServerError().body("Ошибка получения данных пользователя");
        }
    }
}