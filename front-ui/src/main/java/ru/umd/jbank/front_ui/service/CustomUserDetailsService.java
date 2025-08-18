package ru.umd.jbank.front_ui.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Поиск пользователя: {}", username);
        
        var account = accountService.getAccountByUsername(username);
        if (account == null) {
            log.warn("Пользователь не найден: {}", username);
            throw new UsernameNotFoundException("Пользователь не найден: " + username);
        }
        
        log.debug("Пользователь найден: {} (ID: {})", username, account.id());
        
        return User.builder()
            .username(account.username())
            .password(account.password())
            .authorities(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    }
}
