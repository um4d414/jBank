package ru.umd.jbank.notification.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.umd.jbank.notification.service.NotifyService;
import ru.umd.jbank.notification.web.dto.NotifyRequestDto;

@RestController
@RequiredArgsConstructor
public class NotificationController {
    private final NotifyService notifyService;

    @PostMapping("/notify")
    public void notify(@RequestBody NotifyRequestDto notifyRequest) {
        notifyService.sendNotification(
            new NotifyService.NotifyRequest(
                notifyRequest.getAccountId(),
                notifyRequest.getMessage()
            )
        );
    }
}
