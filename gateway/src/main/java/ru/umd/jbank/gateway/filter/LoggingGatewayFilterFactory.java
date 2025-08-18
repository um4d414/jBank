package ru.umd.jbank.gateway.filter;

import lombok.Data;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingGatewayFilterFactory extends AbstractGatewayFilterFactory<LoggingGatewayFilterFactory.Config> {
    public LoggingGatewayFilterFactory() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (config.isLogRequest()) {
                log.info("[{}] Request: {} {} - Headers: {}",
                         config.getServiceName(),
                         exchange.getRequest().getMethod(),
                         exchange.getRequest().getURI(),
                         config.isIncludeHeaders() ? exchange.getRequest().getHeaders() : "HIDDEN"
                );
            }

            return chain
                .filter(exchange)
                .then(
                    Mono.fromRunnable(() -> {
                        if (config.isLogResponse()) {
                            log.info("[{}] Response: {} - Size: {} bytes",
                                     config.getServiceName(),
                                     exchange.getResponse().getStatusCode(),
                                     exchange.getResponse().getHeaders().getContentLength()
                            );
                        }
                    })
                );
        };
    }


    @Data
    public static class Config {
        private String serviceName = "UNKNOWN";
        private boolean logRequest = true;
        private boolean logResponse = true;
        private boolean includeHeaders = false;
    }
}
