package com.example.walletapp;

import com.example.walletapp.model.dto.WalletDTO;
import com.example.walletapp.model.dto.WalletRequestDTO;
import com.example.walletapp.enums.OperationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WalletControllerIT extends AbstractIntegrationTest {

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
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldCreateWallet() throws Exception {

        UUID walletId = UUID.randomUUID();

        WalletDTO request = new WalletDTO();
        request.setWalletId(walletId);
        request.setAmount(BigDecimal.valueOf(1000));

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/wallets").contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.amount").value(1000));
    }

    @Test
    void shouldReturn409WhenWalletAlreadyExists() throws Exception {

        UUID walletId = UUID.randomUUID();

        createWallet(walletId, BigDecimal.valueOf(500));

        WalletDTO request = new WalletDTO();
        request.setWalletId(walletId);
        request.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Wallet already exists"));

        mockMvc.perform(get("/api/v1/wallets/{id}", walletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500));
    }

    @Test
    void shouldDepositMoney() throws Exception {

        UUID walletId = UUID.randomUUID();

        createWallet(walletId, BigDecimal.ZERO);

        WalletRequestDTO request = new WalletRequestDTO();
        request.setWalletId(walletId);
        request.setAmount(BigDecimal.valueOf(1000));
        request.setOperationType(OperationType.DEPOSIT);

        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(
                        post("/api/v1/wallet").contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.walletId").value(walletId.toString()))
                .andExpect(jsonPath("$.amount").value(1000));
    }

    @Test
    void shouldFailWithdrawWhenNotEnoughMoney() throws Exception {

        UUID walletId = UUID.randomUUID();

        createWallet(walletId, BigDecimal.ZERO);

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setWalletId(walletId);
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
        requestDTO.setWalletId(walletId);
        requestDTO.setOperationType(OperationType.WITHDRAW);
        requestDTO.setAmount(BigDecimal.valueOf(500));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1500));
    }

    @Test
    void shouldReturn400WhenOperationTypeNull() throws Exception {

        UUID walletId = UUID.randomUUID();

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setWalletId(walletId);
        requestDTO.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenOperationTypeUnknown() throws Exception {

        String json = """
            {
              "walletId": "%s",
              "operationType": "TRANSFER",
              "amount": 100
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404WhenWalletForOperationNotFound() throws Exception {

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setWalletId(UUID.randomUUID());
        requestDTO.setOperationType(OperationType.DEPOSIT);
        requestDTO.setAmount(BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn400WhenAmountNegative() throws Exception {

        UUID walletId = UUID.randomUUID();

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setWalletId(walletId);
        requestDTO.setOperationType(OperationType.DEPOSIT);
        requestDTO.setAmount(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenAmountHasTooManyDecimals() throws Exception {

        UUID walletId = UUID.randomUUID();

        createWallet(walletId, BigDecimal.ZERO);

        WalletRequestDTO requestDTO = new WalletRequestDTO();
        requestDTO.setWalletId(walletId);
        requestDTO.setOperationType(OperationType.DEPOSIT);
        requestDTO.setAmount(new BigDecimal("10.123"));

        mockMvc.perform(post("/api/v1/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());
    }

    @Test
    void shouldReturn400WhenUnknownFieldPresent() throws Exception {

        // payload операции, отправленный на эндпоинт создания кошелька,
        // должен отклоняться, а не молча создавать кошелёк
        String json = """
            {
              "walletId": "%s",
              "operationType": "WITHDRAW",
              "amount": 500
            }
            """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404ForUnknownPath() throws Exception {

        mockMvc.perform(get("/api/v1/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void shouldReturn405ForUnsupportedMethod() throws Exception {

        mockMvc.perform(delete("/api/v1/wallet"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405));
    }

    private void createWallet(UUID walletId, BigDecimal balance) throws Exception {

        WalletDTO request = new WalletDTO();
        request.setWalletId(walletId);
        request.setAmount(balance);

        mockMvc.perform(
                        post("/api/v1/wallets")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
    }
}
