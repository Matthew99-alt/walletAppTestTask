package com.example.walletapp.controller;

import com.example.walletapp.model.dto.ErrorDTO;
import com.example.walletapp.model.dto.WalletRequestDTO;
import com.example.walletapp.model.dto.WalletDTO;
import com.example.walletapp.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Wallet", description = "Wallet balance operations")
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/wallets/{walletId}")
    @Operation(summary = "Get wallet by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet found"),
            @ApiResponse(responseCode = "404", description = "Wallet not found",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    public WalletDTO getWallet(@PathVariable("walletId") UUID id) {
        return walletService.getWallet(id);
    }

    @PostMapping("/wallet")
    @Operation(summary = "Deposit to or withdraw from a wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Operation applied, current wallet state returned"),
            @ApiResponse(responseCode = "400", description = "Validation error or malformed JSON",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Wallet not found",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "409", description = "Insufficient funds or wallet busy, retry later",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    public WalletDTO changeBalance(@RequestBody @Valid WalletRequestDTO walletRequestDTO) {
        return walletService.changeBalance(walletRequestDTO);
    }

    @PostMapping("/wallets")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Wallet created"),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "409", description = "Wallet with this id already exists",
                    content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    public WalletDTO createWallet(@RequestBody @Valid WalletDTO walletDTO) {
        return walletService.createWallet(walletDTO);
    }

}
