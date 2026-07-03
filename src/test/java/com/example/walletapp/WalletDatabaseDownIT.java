package com.example.walletapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверяет деградацию при недоступной БД: клиент должен получить 503,
 * а не 500 и не минутное зависание. Класс использует собственный контейнер
 * (не общий из AbstractIntegrationTest), потому что стопает его посреди теста.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WalletDatabaseDownIT {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRES.start();
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturn503WhenDatabaseIsDown() {

        POSTGRES.stop();

        // Первые запросы после внезапной смерти БД могут упасть с 500 на
        // мёртвом соединении, которое пул ещё считает живым. После эвикции
        // Hikari не может открыть новое соединение (connection-timeout 5s)
        // и клиент стабильно получает 503.
        ResponseEntity<String> response = null;
        for (int attempt = 0; attempt < 5; attempt++) {
            response = restTemplate.getForEntity(
                    "/api/v1/wallets/" + UUID.randomUUID(), String.class);
            if (response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE) {
                break;
            }
        }

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).contains("Service temporarily unavailable");
    }
}
