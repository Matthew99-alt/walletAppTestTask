package com.example.walletApp;

import com.example.walletApp.model.dto.WalletDTO;
import com.example.walletApp.model.dto.WalletRequestDTO;
import com.example.walletApp.enums.OperationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class WalletControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("wallet_db")
                    .withUsername("postgres")
                    .withPassword("1234");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturn404WhenWalletNotFound() throws Exception {

        UUID fakeId = UUID.randomUUID();

        mockMvc.perform(get("/api/v1/wallets/{id}", fakeId))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Wallet not found"))
                .andExpect(jsonPath("$.number").value(404));
    }

    @Test
    @Transactional
    void shouldCreateWallet() throws Exception {

        UUID walletId = UUID.randomUUID();

        WalletRequestDTO request = new WalletRequestDTO();
        request.setValletId(walletId);
        request.setAmount(BigDecimal.valueOf(1000));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/saveWallet").contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void shouldDepositMoney() throws Exception {

        UUID walletId = UUID.randomUUID();

        createWallet(walletId);

        WalletRequestDTO request = new WalletRequestDTO();
        request.setValletId(walletId);
        request.setAmount(BigDecimal.valueOf(1000));
        request.setOperationType(OperationType.DEPOSIT);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/wallet").contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                );
    }

    @Test
    void shouldFailWithdrawWhenNotEnoughMoney() throws Exception {

        UUID walletId = UUID.randomUUID();

        createWallet(walletId);

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setValletId(walletId);
        requestDTO.setOperationType(OperationType.WITHDRAW);
        requestDTO.setAmount(BigDecimal.valueOf(1000));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenJsonInvalid() throws Exception {

        String invalidJson = """
            {
              "id": "abc",
              "operationType": "DEPOSIT",
              "balance":
            }
            """;

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldWithdrawMoney() throws Exception {

        UUID walletId = UUID.randomUUID();

        createWallet(walletId, BigDecimal.valueOf(2000));

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setValletId(walletId);
        requestDTO.setOperationType(OperationType.WITHDRAW);
        requestDTO.setAmount(BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenOperationTypeNull() throws Exception {

        UUID walletId = UUID.randomUUID();

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setValletId(walletId);
        requestDTO.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenWalletForOperationNotFound() throws Exception {

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setValletId(UUID.randomUUID());
        requestDTO.setOperationType(OperationType.DEPOSIT);
        requestDTO.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenBalanceNegative() throws Exception {

        UUID walletId = UUID.randomUUID();

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setValletId(walletId);
        requestDTO.setOperationType(OperationType.DEPOSIT);
        requestDTO.setAmount(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    private void createWallet(UUID walletId) throws Exception {

        WalletRequestDTO request = new WalletRequestDTO();
        request.setValletId(walletId);
        request.setAmount(BigDecimal.valueOf(0));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                post("/api/v1/saveWallet").contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        );
    }

    private void createWallet(UUID walletId, BigDecimal balance) throws Exception {

        WalletDTO request = new WalletDTO();
        request.setValletId(walletId);
        request.setAmount(balance);

        mockMvc.perform(
                post("/api/v1/saveWallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );
    }
}