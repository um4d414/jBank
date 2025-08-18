package ru.umd.jbank.front_ui.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.umd.jbank.front_ui.service.AccountService;
import ru.umd.jbank.front_ui.web.dto.UserRegistrationDto;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AccountService accountService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "auth/logout";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("userRegistration", new UserRegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(
        @Valid @ModelAttribute("userRegistration") UserRegistrationDto userDto,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (userDto.getBirthDate() != null) {
            int age = Period.between(userDto.getBirthDate(), LocalDate.now()).getYears();
            if (age < 18) {
                bindingResult.rejectValue("birthDate", "age.invalid", "Возраст должен быть не менее 18 лет");
            }
        }

        if (userDto.getPassword() != null && !userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Пароли не совпадают");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            var createdAccount = accountService.createAccount(userDto);
            log.info("Создан новый аккаунт: {} (ID: {})", createdAccount.username(), createdAccount.id());

            var authentication = new UsernamePasswordAuthenticationToken(
                createdAccount.username(),
                null,
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            redirectAttributes.addFlashAttribute("success", "Регистрация прошла успешно! Добро пожаловать!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            log.error("Ошибка при регистрации пользователя: {}", userDto.getUsername(), e);
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "auth/register";
        }
    }

}