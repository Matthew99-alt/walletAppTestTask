package com.example.walletApp.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class WalletDTO {
    @NotNull
    private UUID valletId;

    @NotNull
    @PositiveOrZero
    private BigDecimal amount;
}
