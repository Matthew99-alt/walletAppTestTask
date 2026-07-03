package com.example.walletapp.mapper;

import com.example.walletapp.model.dto.WalletDTO;
import com.example.walletapp.model.entity.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletDTO walletToDTO(Wallet wallet){
        WalletDTO walletDTO = new WalletDTO();

        walletDTO.setWalletId(wallet.getWalletId());
        walletDTO.setAmount(wallet.getAmount());

        return walletDTO;
    }

    public Wallet dtoToWallet(WalletDTO walletDTO){
        Wallet wallet = new Wallet();

        wallet.setWalletId(walletDTO.getWalletId());
        wallet.setAmount(walletDTO.getAmount());

        return wallet;
    }
}
