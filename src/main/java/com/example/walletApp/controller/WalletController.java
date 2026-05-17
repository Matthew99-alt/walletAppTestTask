package com.example.walletApp.controller;

import com.example.walletApp.exception.InsufficientFundsException;
import com.example.walletApp.model.dto.WalletRequestDTO;
import com.example.walletApp.model.dto.WalletDTO;
import com.example.walletApp.service.WalletService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/wallets/{WALLET_UUID}")
    public WalletDTO getAWallet(@PathVariable("WALLET_UUID") UUID id) {
        return walletService.getAWallet(id);
    }

    @PostMapping("/wallet")
    public WalletDTO changeBalance(@RequestBody @Valid WalletRequestDTO walletRequestDTO) throws InsufficientFundsException, EntityNotFoundException {
        return walletService.changeBalance(walletRequestDTO);
    }

    @PostMapping("/saveWallet")
    public WalletDTO saveAWallet(@RequestBody @Valid WalletDTO walletDTO) {
        return walletService.saveAWallet(walletDTO);
    }

}
