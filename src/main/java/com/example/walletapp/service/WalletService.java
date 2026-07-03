package com.example.walletapp.service;

import com.example.walletapp.exception.InsufficientFundsException;
import com.example.walletapp.exception.WalletAlreadyExistsException;
import com.example.walletapp.mapper.WalletMapper;
import com.example.walletapp.model.dto.WalletRequestDTO;
import com.example.walletapp.model.dto.WalletDTO;
import com.example.walletapp.model.entity.Wallet;
import com.example.walletapp.repository.WalletRepository;
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
    public WalletDTO getWallet(UUID uuid){
        Wallet requiredWallet = walletRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Wallet not found"));

        return walletMapper.walletToDTO(requiredWallet);
    }

    @Transactional
    public WalletDTO createWallet(WalletDTO walletDTO){
        if (walletRepository.existsById(walletDTO.getWalletId())) {
            throw new WalletAlreadyExistsException("Wallet already exists");
        }

        walletRepository.save(walletMapper.dtoToWallet(walletDTO));

        return walletDTO;
    }
}
