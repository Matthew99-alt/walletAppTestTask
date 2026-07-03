package com.example.walletApp.controller;

import com.example.walletApp.model.dto.WalletRequestDTO;
import com.example.walletApp.model.dto.WalletDTO;
import com.example.walletApp.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/wallets/{walletId}")
    public WalletDTO getAWallet(@PathVariable("walletId") UUID id) {
        return walletService.getAWallet(id);
    }

    @PostMapping("/wallet")
    public WalletDTO changeBalance(@RequestBody @Valid WalletRequestDTO walletRequestDTO) {
        return walletService.changeBalance(walletRequestDTO);
    }

    @PostMapping("/wallets")
    @ResponseStatus(HttpStatus.CREATED)
    public WalletDTO saveAWallet(@RequestBody @Valid WalletDTO walletDTO) {
        return walletService.saveAWallet(walletDTO);
    }

}
