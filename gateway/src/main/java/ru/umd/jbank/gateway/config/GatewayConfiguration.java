package ru.umd.jbank.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfiguration {
    @Bean
    public WebClient webClient() {
        return WebClient
            .builder()
            .codecs(
                configurer -> configurer
                    .defaultCodecs()
                    .maxInMemorySize(1024 * 1024)
            )
            .build();
    }
}
