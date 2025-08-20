package ru.umd.jbank.keycloak.autoconfigure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import ru.umd.jbank.keycloak.properties.KeycloakProperties;

@Configuration
@ConditionalOnClass(JwtDecoder.class)
@ConditionalOnMissingClass("org.springframework.cloud.gateway.config.GatewayAutoConfiguration") // Исключаем Gateway
@ConditionalOnProperty(prefix = "jbank.keycloak.resource-server", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(KeycloakProperties.class)
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class KeycloakResourceServerAutoConfiguration {
    private final KeycloakProperties properties;

    @Bean
    @Order(1)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Настройка SecurityFilterChain для Keycloak Resource Server");

        http
            .securityMatcher(new NegatedRequestMatcher(new AntPathRequestMatcher("/actuator/**")))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                                   session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> {
                String[] publicPaths = properties.getResourceServer().getPublicPaths();
                for (String path : publicPaths) {
                    if (!path.startsWith("/actuator")) {
                        authz.requestMatchers(path).permitAll();
                    }
                }
                authz.anyRequest().authenticated();
            })
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = properties.getServerUrl() + "/realms/" + properties.getRealm() + "/protocol/openid-connect/certs";
        log.info("Настройка JwtDecoder с JWK Set URI: {}", jwkSetUri);
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}