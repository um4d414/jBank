package ru.umd.jbank.keycloak.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import ru.umd.jbank.keycloak.properties.KeycloakProperties;

@RequiredArgsConstructor
@Slf4j
public class KeycloakFeignInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String BEARER_TOKEN_TYPE = "Bearer";

    private final OAuth2AuthorizedClientManager clientManager;

    private final KeycloakProperties properties;

    @Override
    public void apply(RequestTemplate template) {
        try {
            OAuth2AuthorizeRequest oAuth2AuthorizeRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId(properties.getClient().getRegistrationId())
                .principal(properties.getClient().getPrincipalName())
                .build();

            var client = clientManager.authorize(oAuth2AuthorizeRequest);

            if (client != null && client.getAccessToken() != null) {
                template.header(
                    AUTHORIZATION_HEADER,
                    String.format("%s %s", BEARER_TOKEN_TYPE, client.getAccessToken().getTokenValue())
                );
                log.debug("OAuth2 токен добавлен к Feign запросу для межсервисного взаимодействия");
            } else {
                log.warn("Не удалось получить OAuth2 токен для межсервисного взаимодействия");
            }
        } catch (Exception e) {
            log.error("Ошибка при добавлении OAuth2 токена к Feign запросу", e);
        }
    }
}
