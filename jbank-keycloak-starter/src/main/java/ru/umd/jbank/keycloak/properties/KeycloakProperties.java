package ru.umd.jbank.keycloak.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "jbank.keycloak")
public class KeycloakProperties {
    
    /**
     * Base URL Keycloak сервера
     */
    private String serverUrl = "http://localhost:8080";
    
    /**
     * Имя realm
     */
    private String realm = "jbank";
    
    /**
     * Настройки для клиента (отправка запросов)
     */
    private Client client = new Client();
    
    /**
     * Настройки для resource server (проверка токенов)
     */
    private ResourceServer resourceServer = new ResourceServer();
    
    /**
     * Включить автоконфигурацию для Feign клиентов
     */
    private boolean enableFeignInterceptor = true;
    
    @Data
    public static class Client {
        /**
         * Client ID для межсервисного взаимодействия
         */
        private String clientId;
        
        /**
         * Client Secret
         */
        private String clientSecret;
        
        /**
         * Registration ID для OAuth2 клиента
         */
        private String registrationId = "service-client";
        
        /**
         * Principal name для service account
         */
        private String principalName;
    }
    
    @Data
    public static class ResourceServer {
        /**
         * Включить проверку JWT токенов
         */
        private boolean enabled = true;
        
        /**
         * Пути, которые не требуют аутентификации
         */
        private String[] publicPaths = {"/actuator/**"};
    }
}
