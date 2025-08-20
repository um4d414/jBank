package ru.umd.jbank.keycloak.autoconfigure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import ru.umd.jbank.keycloak.properties.KeycloakProperties;

@Configuration
@ConditionalOnClass(OAuth2AuthorizedClientManager.class)
@ConditionalOnProperty(prefix = "jbank.keycloak.client", name = "client-id")
@EnableConfigurationProperties(KeycloakProperties.class)
@RequiredArgsConstructor
@Slf4j
public class KeycloakClientAutoConfiguration {
    private final KeycloakProperties properties;

    @Bean
    @ConditionalOnProperty(prefix = "jbank.keycloak.client", name = "client-id")
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId(properties.getClient().getRegistrationId())
                .clientId(properties.getClient().getClientId())
                .clientSecret(properties.getClient().getClientSecret())
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .tokenUri(properties.getServerUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/token")
                .scope("openid")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jbank.keycloak.client", name = "client-id")
    public OAuth2AuthorizedClientService authorizedClientService(ClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jbank.keycloak.client", name = "client-id")
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = 
            OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = 
            new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, 
                authorizedClientService
            );
        
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        
        log.info("Настроен OAuth2AuthorizedClientManager для клиента: {}", 
                properties.getClient().getClientId());
        
        return authorizedClientManager;
    }
}
