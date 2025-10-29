package com.example.bankrest.controller;

import com.example.bankrest.controller.api.TransactionApi;
import com.example.bankrest.dto.TransferRequestDto;
import com.example.bankrest.security.UserDetailsImpl;
import com.example.bankrest.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TransactionController implements TransactionApi {

    private final TransactionService transactionService;

    @Override
    public ResponseEntity<Void> transferFunds(Authentication authentication, TransferRequestDto transferRequestDto) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.user().getId();
        transactionService.transferFunds(transferRequestDto, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
