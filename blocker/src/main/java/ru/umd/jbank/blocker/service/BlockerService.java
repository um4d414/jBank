package ru.umd.jbank.blocker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class BlockerService {
    @Value("${blocker.validation.success-rate}")
    private int successRatePercent;

    public boolean validate() {
        return ThreadLocalRandom.current().nextInt(100) < successRatePercent;
    }
}
