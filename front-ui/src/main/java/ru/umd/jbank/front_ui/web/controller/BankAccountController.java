package ru.umd.jbank.front_ui.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.umd.jbank.front_ui.service.AccountService;
import ru.umd.jbank.front_ui.service.BankAccountService;

import java.util.Currency;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BankAccountController {
    private final AccountService accountService;

    private final BankAccountService bankAccountService;
    
    @PostMapping("/account/add-bank-account")
    public String addBankAccount(@RequestParam("currency") String currencyCode,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            log.info("Создание банковского счета в валюте {} для пользователя {}", 
                     currencyCode, username);
            
            var currentUser = accountService.getAccountByUsername(username);
            
            var currency = Currency.getInstance(currencyCode);
            
            var createdAccount = bankAccountService.createBankAccount(currentUser.id(), currency);
            
            redirectAttributes.addFlashAttribute("success", 
                "Банковский счет в валюте " + currency + " успешно создан!");
            
        } catch (IllegalArgumentException e) {
            log.error("Неверная валюта: {}", currencyCode, e);
            redirectAttributes.addFlashAttribute("error", "Неверная валюта: " + currencyCode);
            
        } catch (Exception e) {
            log.error("Ошибка при создании банковского счета", e);
            
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("already exists")) {
                redirectAttributes.addFlashAttribute("error", 
                    "У вас уже есть счет в валюте " + currencyCode);
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Ошибка при создании банковского счета: " + errorMessage);
            }
        }
        
        return "redirect:/dashboard";
    }

    @PostMapping("/account/delete-bank-account")
    public String deleteBankAccount(@RequestParam("accountId") Long bankAccountId,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            log.info("Удаление банковского счета {} для пользователя {}", bankAccountId, username);
            
            // TODO: Реализовать удаление банковского счета
            // var currentUser = accountService.getAccountByUsername(username);
            // bankAccountService.deleteBankAccount(bankAccountId, currentUser.id());
            
            redirectAttributes.addFlashAttribute("success", "Банковский счет удален!");
            
        } catch (Exception e) {
            log.error("Ошибка при удалении банковского счета", e);
            redirectAttributes.addFlashAttribute("error", 
                "Ошибка при удалении банковского счета: " + e.getMessage());
        }
        
        return "redirect:/dashboard";
    }
}
