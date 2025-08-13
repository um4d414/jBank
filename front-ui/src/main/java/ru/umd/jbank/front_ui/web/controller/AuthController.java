package ru.umd.jbank.front_ui.web.controller;

import ru.umd.jbank.front_ui.web.dto.UserRegistrationDto;
import jakarta.validation.Valid;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;

@RestController
public class AuthController {
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

        // Проверка возраста (18+)
        if (userDto.getBirthDate() != null) {
            int age = Period.between(userDto.getBirthDate(), LocalDate.now()).getYears();
            if (age < 18) {
                bindingResult.rejectValue("birthDate", "age.invalid", "Возраст должен быть не менее 18 лет");
            }
        }

        // Проверка совпадения паролей
        if (userDto.getPassword() != null && !userDto.getPassword().equals(userDto.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Пароли не совпадают");
        }

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            // Здесь должна быть логика регистрации пользователя через сервис
            // userService.registerUser(userDto);

            // Временная заглушка для демонстрации
            System.out.println("Регистрация пользователя: " + userDto.getUsername());
            System.out.println("Email: " + userDto.getEmail());
            System.out.println("ФИО: " + userDto.getLastName() + " " + userDto.getFirstName());

            redirectAttributes.addFlashAttribute("success", "Регистрация прошла успешно! Добро пожаловать!");
            return "redirect:/dashboard";

        } catch (Exception e) {
            model.addAttribute("error", "Ошибка при регистрации: " + e.getMessage());
            return "auth/register";
        }
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }
}
