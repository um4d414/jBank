package ru.umd.jbank.front_ui.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.umd.jbank.front_ui.service.AccountService;
import ru.umd.jbank.front_ui.service.BankAccountService;

import java.util.Currency;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AccountController {
    private final AccountService accountService;

    private final BankAccountService bankAccountService;

    @PostMapping("/account/add-bank-account")
    public String addBankAccount(
        @RequestParam("currency") String currencyCode,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        try {
            String username = authentication.getName();
            log.info(
                "Создание банковского счета в валюте {} для пользователя {}",
                currencyCode, username
            );

            var currentUser = accountService.getAccountByUsername(username);
            var currency = Currency.getInstance(currencyCode);
            var createdAccount = bankAccountService.createBankAccount(currentUser.id(), currency);

            redirectAttributes.addFlashAttribute(
                "success",
                "Банковский счет в валюте " + currency + " успешно создан!"
            );

        } catch (IllegalArgumentException e) {
            log.error("Неверная валюта: {}", currencyCode, e);
            redirectAttributes.addFlashAttribute("error", "Неверная валюта: " + currencyCode);

        } catch (Exception e) {
            log.error("Ошибка при создании банковского счета", e);
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("already exists")) {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "У вас уже есть счет в валюте " + currencyCode
                );
            } else {
                redirectAttributes.addFlashAttribute(
                    "error",
                    "Ошибка при создании банковского счета: " + errorMessage
                );
            }
        }

        return "redirect:/dashboard";
    }

    @PostMapping("/account/delete-bank-account")
    public String deleteBankAccount(
        @RequestParam("accountId") Long bankAccountId,
        Authentication authentication,
        RedirectAttributes redirectAttributes
    ) {
        try {
            String username = authentication.getName();
            log.info("Удаление банковского счета {} для пользователя {}", bankAccountId, username);

            // TODO: Реализовать удаление банковского счета
            // var currentUser = accountService.getAccountByUsername(username);
            // bankAccountService.deleteBankAccount(bankAccountId, currentUser.id());

            redirectAttributes.addFlashAttribute("success", "Банковский счет удален!");

        } catch (Exception e) {
            log.error("Ошибка при удалении банковского счета", e);
            redirectAttributes.addFlashAttribute(
                "error",
                "Ошибка при удалении банковского счета: " + e.getMessage()
            );
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/api/accounts/search-receiver/{username}")
    @ResponseBody
    public ResponseEntity<?> searchReceiver(@PathVariable String username, Authentication authentication) {
        try {
            String currentUser = authentication.getName();
            log.info("Поиск получателя '{}' пользователем '{}'", username, currentUser);

            if (username.equals(currentUser)) {
                return ResponseEntity.badRequest().body("Нельзя переводить самому себе");
            }

            var receiverAccount = accountService.getAccountByUsername(username);
            if (receiverAccount == null) {
                return ResponseEntity.notFound().build();
            }

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

    @GetMapping("/api/accounts/current")
    @ResponseBody
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
