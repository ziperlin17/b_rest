package com.example.bankrest.service;

import com.example.bankrest.dto.AccountResponseDto;
import com.example.bankrest.dto.CreateAccountRequestDto;
import java.util.List;

public interface AccountService {
    AccountResponseDto createAccount(CreateAccountRequestDto createDto, Long userId);
    List<AccountResponseDto> getUserAccounts(Long userId);
}