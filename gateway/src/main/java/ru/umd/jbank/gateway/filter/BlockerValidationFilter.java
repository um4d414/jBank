package ru.umd.jbank.gateway.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlockerValidationFilter implements GlobalFilter, Ordered {
    private final WebClient webClient;

    private final ReactiveDiscoveryClient discoveryClient;

    private static final String BLOCKER_SERVICE_ID = "blocker-service";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var path = exchange.getRequest().getPath().toString();

        if (path.startsWith("/cash/") || path.startsWith("/transfer/")) {
            log.info("Проверка через blocker для пути: {}", path);

            return validateWithBlocker()
                .flatMap(isValid -> {
                    if (isValid) {
                        log.info("Валидация прошла успешно, продолжаем выполнение");
                        return chain.filter(exchange);
                    } else {
                        log.warn("Валидация не прошла, блокируем операцию");
                        return handleBlockedRequest(exchange);
                    }
                })
                .onErrorResume(error -> {
                    log.error("Ошибка при валидации: {}", error.getMessage());
                    return handleBlockerError(exchange);
                });
        }

        return chain.filter(exchange);
    }

    private Mono<Boolean> validateWithBlocker() {
        return getBlockerServiceUrl()
            .flatMap(serviceUrl -> {
                log.debug("Отправка запроса в blocker-service по адресу: {}", serviceUrl);

                return webClient
                    .post()
                    .uri(serviceUrl + "/validate")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(BlockerResponse.class)
                    .map(BlockerResponse::valid)
                    .timeout(Duration.ofSeconds(5));
            })
            .doOnError(error -> log.error("Ошибка при обращении к blocker-service: {}", error.getMessage()))
            .onErrorReturn(false); // В случае ошибки блокируем операцию
    }

    private Mono<String> getBlockerServiceUrl() {
        return discoveryClient.getInstances(BLOCKER_SERVICE_ID)
            .next() // Получаем первый доступный экземпляр
            .map(this::buildServiceUrl)
            .doOnNext(url -> log.debug("Найден blocker-service: {}", url))
            .switchIfEmpty(Mono.error(new RuntimeException("Blocker service не найден в реестре сервисов")));
    }

    private String buildServiceUrl(ServiceInstance instance) {
        return String.format("http://%s:%d", instance.getHost(), instance.getPort());
    }

    private Mono<Void> handleBlockedRequest(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String errorResponse = """
            {
                "error": "OPERATION_BLOCKED",
                "message": "Операция заблокирована системой безопасности",
                "timestamp": "%s"
            }
            """.formatted(java.time.Instant.now());

        var buffer = response.bufferFactory().wrap(errorResponse.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    private Mono<Void> handleBlockerError(ServerWebExchange exchange) {
        var response = exchange.getResponse();
        response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String errorResponse = """
            {
                "error": "VALIDATION_SERVICE_UNAVAILABLE",
                "message": "Сервис валидации временно недоступен",
                "timestamp": "%s"
            }
            """.formatted(java.time.Instant.now());

        var buffer = response.bufferFactory().wrap(errorResponse.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private record BlockerResponse(boolean valid, String message) {}
}
