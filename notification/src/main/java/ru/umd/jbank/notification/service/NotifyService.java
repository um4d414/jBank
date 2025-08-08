package ru.umd.jbank.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.umd.jbank.notification.integration.client.account.AccountClient;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotifyService {
    private final AccountClient accountClient;

    public void sendNotification(NotifyRequest notifyRequest) {
        var email = accountClient.getAccount(notifyRequest.accountId);

        log.info("Sending notification to {} with body {}", email, notifyRequest.message());
    }

    public record NotifyRequest(
        Long accountId,
        String message
    ) {}
}
