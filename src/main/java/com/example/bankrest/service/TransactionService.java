package com.example.bankrest.service;

import com.example.bankrest.dto.TransferRequestDto;

public interface TransactionService {
    void transferFunds(TransferRequestDto transferDto, Long userId);
}
