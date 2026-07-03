package com.example.walletapp.model.dto;

import com.example.walletapp.enums.OperationType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class WalletRequestDTO {
    @NotNull
    private UUID walletId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private OperationType operationType;
}
