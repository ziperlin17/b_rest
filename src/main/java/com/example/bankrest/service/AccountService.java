package com.example.bankrest.service;

import com.example.bankrest.dto.AccountResponseDto;
import com.example.bankrest.dto.CreateAccountRequestDto;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    AccountResponseDto createAccount(CreateAccountRequestDto createDto, Long userId);
    List<AccountResponseDto> getUserAccounts(Long userId);
    AccountResponseDto getAccountByUuid(UUID uuid, Long userId);
    void closeAccount(UUID uuid, Long userId);
}