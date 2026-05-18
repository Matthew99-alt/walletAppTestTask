package com.example.walletApp.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @Column(name = "vallet_id", nullable = false)
    private UUID valletId;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

}
