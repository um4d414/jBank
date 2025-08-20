package ru.umd.jbank.front_ui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.umd.jbank.front_ui.integration.client.account.AccountClient;
import ru.umd.jbank.front_ui.integration.client.account.dto.AccountDto;
import ru.umd.jbank.front_ui.integration.client.account.dto.CreateAccountRequestDto;
import ru.umd.jbank.front_ui.web.dto.UserRegistrationDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountClient accountClient;

    private final PasswordEncoder passwordEncoder;

    public AccountDto createAccount(UserRegistrationDto userDto) {
        var hashedPassword = passwordEncoder.encode(userDto.getPassword());

        var createRequest = new CreateAccountRequestDto(
            userDto.getLastName(),
            userDto.getFirstName(),
            userDto.getEmail(),
            userDto.getBirthDate(),
            userDto.getUsername(),
            hashedPassword
        );

        return accountClient.createAccount(createRequest);
    }

    public AccountDto getAccountByUsername(String username) {
        try {
            return accountClient.getAccountByUsername(username);
        } catch (Exception e) {
            log.error("Can't find account by username: {}", username, e);
            return null;
        }
    }

    public AccountDto getAccount(Long id) {
        try {
            return accountClient.getAccount(id);
        } catch (Exception e) {
            log.error("Can't find account by id: {}", id, e);
            return null;
        }
    }

    public Long getBankAccountOwnerId(Long bankAccountId) {
        try {
            log.debug("Поиск владельца банковского счета: {}", bankAccountId);
            Long ownerId = accountClient.getBankAccountOwnerId(bankAccountId);
            log.debug("Владелец банковского счета {}: {}", bankAccountId, ownerId);
            return ownerId;
        } catch (Exception e) {
            log.error("Ошибка при поиске владельца банковского счета: {}", bankAccountId, e);
            throw new RuntimeException("Не удалось найти владельца банковского счета", e);
        }
    }

}

