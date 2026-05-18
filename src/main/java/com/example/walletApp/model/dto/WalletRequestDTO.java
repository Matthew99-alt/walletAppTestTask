package com.example.walletApp.model.dto;

import com.example.walletApp.enums.OperationType;
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
    private UUID valletId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private OperationType operationType;
}
