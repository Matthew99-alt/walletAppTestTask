package com.example.walletapp;

import com.example.walletapp.enums.OperationType;
import com.example.walletapp.model.dto.WalletDTO;
import com.example.walletapp.model.dto.WalletRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверяет корректность баланса под конкурентной нагрузкой:
 * параллельные списания не должны терять обновления и уводить баланс в минус.
 * Тест ходит по реальному HTTP, чтобы запросы обрабатывались разными потоками сервера.
 */
class WalletConcurrencyIT extends AbstractIntegrationTest {

    private static final BigDecimal INITIAL_BALANCE = BigDecimal.valueOf(1000);
    private static final BigDecimal WITHDRAWAL = BigDecimal.TEN;
    private static final int ATTEMPTS = 150;
    private static final int THREADS = 20;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void concurrentWithdrawalsDoNotLoseUpdatesOrGoNegative() throws Exception {

        UUID walletId = UUID.randomUUID();

        WalletDTO createRequest = new WalletDTO();
        createRequest.setWalletId(walletId);
        createRequest.setAmount(INITIAL_BALANCE);

        ResponseEntity<WalletDTO> created =
                restTemplate.postForEntity("/api/v1/wallets", createRequest, WalletDTO.class);
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicInteger successCount = new AtomicInteger();
        ConcurrentLinkedQueue<Integer> unexpectedStatuses = new ConcurrentLinkedQueue<>();

        try {
            List<Future<?>> futures = new ArrayList<>();

            for (int i = 0; i < ATTEMPTS; i++) {
                futures.add(executor.submit(() -> {
                    startSignal.await();

                    WalletRequestDTO request = new WalletRequestDTO();
                    request.setWalletId(walletId);
                    request.setOperationType(OperationType.WITHDRAW);
                    request.setAmount(WITHDRAWAL);

                    ResponseEntity<String> response =
                            restTemplate.postForEntity("/api/v1/wallet", request, String.class);

                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    } else if (response.getStatusCode() != HttpStatus.CONFLICT) {
                        // допустимы только 200 (успех) и 409 (нет средств / таймаут блокировки)
                        unexpectedStatuses.add(response.getStatusCode().value());
                    }

                    return null;
                }));
            }

            startSignal.countDown();

            for (Future<?> future : futures) {
                future.get(60, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(unexpectedStatuses).isEmpty();

        ResponseEntity<WalletDTO> after =
                restTemplate.getForEntity("/api/v1/wallets/" + walletId, WalletDTO.class);
        assertThat(after.getStatusCode()).isEqualTo(HttpStatus.OK);

        BigDecimal finalBalance = after.getBody().getAmount();
        BigDecimal expectedBalance = INITIAL_BALANCE
                .subtract(WITHDRAWAL.multiply(BigDecimal.valueOf(successCount.get())));

        // при потерянных обновлениях итоговый баланс не сойдётся с числом успешных списаний
        assertThat(finalBalance).isEqualByComparingTo(expectedBalance);
        assertThat(finalBalance).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }
}
