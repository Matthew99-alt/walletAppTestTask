package com.example.walletApp.service;

import com.example.walletApp.exception.InsufficientFundsException;
import com.example.walletApp.exception.WalletAlreadyExistsException;
import com.example.walletApp.mapper.WalletMapper;
import com.example.walletApp.model.dto.WalletRequestDTO;
import com.example.walletApp.model.dto.WalletDTO;
import com.example.walletApp.model.entity.Wallet;
import com.example.walletApp.repository.WalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    @Transactional
    public WalletDTO changeBalance(WalletRequestDTO walletRequestDTO) {
        Wallet walletToChange = walletRepository.findByIdForUpdate(walletRequestDTO.getWalletId())
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        switch(walletRequestDTO.getOperationType()){
            case DEPOSIT -> walletToChange.setAmount(walletToChange.getAmount().add(walletRequestDTO.getAmount()));
            case WITHDRAW -> {
                if (walletToChange.getAmount().compareTo(walletRequestDTO.getAmount())<0){
                    throw new InsufficientFundsException("Negative balance");
                }
                walletToChange.setAmount(walletToChange.getAmount().subtract(walletRequestDTO.getAmount()));
            }
        }

        return walletMapper.walletToDTO(walletToChange);
    }

    @Transactional(readOnly = true)
    public WalletDTO getAWallet(UUID uuid){
        Wallet requiredWallet = walletRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        return walletMapper.walletToDTO(requiredWallet);
    }

    @Transactional
    public WalletDTO saveAWallet(WalletDTO walletDTO){
        if (walletRepository.existsById(walletDTO.getWalletId())) {
            throw new WalletAlreadyExistsException("Wallet already exists");
        }

        walletRepository.save(walletMapper.dtoToWallet(walletDTO));

        return walletDTO;
    }
}
