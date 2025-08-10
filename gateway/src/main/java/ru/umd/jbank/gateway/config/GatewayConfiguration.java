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
        return builder.routes()
            .route(
                "account-route", r -> r
                    .path("/account/**")
                    .uri("http://localhost:9081")
            )
            .route(
                "cash-route", r -> r
                    .path("/cash/**")
                    .uri("http://localhost:9082")
            )
            .route(
                "blocker-route", r -> r
                    .path("/validate/**")
                    .uri("http://localhost:9083")
            )
            .route(
                "exchange-route", r -> r
                    .path("/exchange/**")
                    .uri("http://localhost:9084")
            )
            .route(
                "notification-route", r -> r
                    .path("/notify/**")
                    .uri("http://localhost:9085")
            )
            .route(
                "transfer-route", r -> r
                    .path("/transfer/**")
                    .uri("http://localhost:9087")
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
