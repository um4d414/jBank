package ru.umd.jbank.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfiguration {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder
            .routes()
            .route("account-route", r -> r
                .path("/account/**")
                .or()
                .path("/bank-account/**")
                .uri("lb://account-service")
            )
            .route("cash-route", r -> r
                .path("/cash/**")
                .uri("lb://cash-service")
            )
            .route("blocker-route", r -> r
                .path("/validate/**")
                .uri("lb://blocker-service")
            )
            .route("exchange-route", r -> r
                .path("/exchange/**")
                .uri("lb://exchange-service")
            )
            .route("notification-route", r -> r
                .path("/notify/**")
                .uri("lb://notification-service")
            )
            .route("transfer-route", r -> r
                .path("/transfer/**")
                .uri("lb://transfer-service")
            )
            .build();
    }

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
