package com.example.walletApp.mapper;

import com.example.walletApp.model.dto.WalletDTO;
import com.example.walletApp.model.entity.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletMapper {

    public WalletDTO walletToDTO(Wallet wallet){
        WalletDTO walletDTO = new WalletDTO();

        walletDTO.setValletId(wallet.getValletId());
        walletDTO.setAmount(wallet.getAmount());

        return walletDTO;
    }

    public Wallet dtoToWallet(WalletDTO walletDTO){
        Wallet wallet = new Wallet();

        wallet.setValletId(walletDTO.getValletId());
        wallet.setAmount(walletDTO.getAmount());

        return wallet;
    }
}
