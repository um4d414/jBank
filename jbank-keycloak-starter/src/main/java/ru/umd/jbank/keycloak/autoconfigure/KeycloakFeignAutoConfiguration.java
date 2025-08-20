package ru.umd.jbank.keycloak.autoconfigure;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import ru.umd.jbank.keycloak.client.KeycloakFeignInterceptor;
import ru.umd.jbank.keycloak.properties.KeycloakProperties;

@Configuration
@ConditionalOnClass(RequestInterceptor.class)
@ConditionalOnProperty(prefix = "jbank.keycloak", name = "enable-feign-interceptor", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KeycloakProperties.class)
@RequiredArgsConstructor
@Slf4j
public class KeycloakFeignAutoConfiguration {
    private final KeycloakProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "jbank.keycloak.client", name = "client-id")
    public RequestInterceptor keycloakFeignRequestInterceptor(OAuth2AuthorizedClientManager clientManager) {
        log.info("Создание Keycloak Feign Request Interceptor для клиента: {}", 
                properties.getClient().getClientId());
        return new KeycloakFeignInterceptor(clientManager, properties);
    }
}
