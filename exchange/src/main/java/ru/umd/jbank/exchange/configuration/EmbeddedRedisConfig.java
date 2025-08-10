package ru.umd.jbank.exchange.configuration;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;

@Configuration
@Slf4j
public class EmbeddedRedisConfig {
    @Value("${spring.redis.port:6379}")
    private int redisPort;

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() throws IOException {
        log.info("Запуск встроенного Redis на порту {}", redisPort);
        redisServer = RedisServer.builder()
            .port(redisPort)
            .setting("maxmemory 128M")
            .build();
        redisServer.start();
        log.info("Встроенный Redis успешно запущен");
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null) {
            log.info("Остановка встроенного Redis");
            redisServer.stop();
        }
    }

}
