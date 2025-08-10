package ru.umd.jbank.exchange_generator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeGeneratorScheduler {
    private final ExchangeGeneratorService exchangeGeneratorService;

    @Scheduled(cron = "*/1 * * * * ?")
    public void publishRatesSchedule() {
        try {
            log.debug("Публикация курсов валют по расписанию");
            exchangeGeneratorService.publishRates();
        } catch (Exception e) {
            log.error("Ошибка при публикации курсов: {}", e.getMessage());
        }
    }
}
