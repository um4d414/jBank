package ru.umd.jbank.exchange_generator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableDiscoveryClient
@RefreshScope
public class ExchangeGeneratorApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeGeneratorApplication.class, args);
    }
}
