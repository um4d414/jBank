package ru.umd.jbank.notification.web.dto;

import lombok.Data;

@Data
public class NotifyRequestDto {
    private String message;

    private Long accountId;
}
