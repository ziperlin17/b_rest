package com.example.bankrest.controller;
import com.example.bankrest.controller.api.AccountApi;
import com.example.bankrest.dto.AccountResponseDto;
import com.example.bankrest.dto.CreateAccountRequestDto;
import com.example.bankrest.security.UserDetailsImpl;
import com.example.bankrest.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController implements AccountApi {

    private final AccountService accountService;

    @Override
    public ResponseEntity<List<AccountResponseDto>> getCurrentUserAccounts(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.user().getId();
        return ResponseEntity.ok(accountService.getUserAccounts(currentUserId));
    }

    @Override
    public ResponseEntity<AccountResponseDto> createAccount(Authentication authentication, CreateAccountRequestDto createAccountDto) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.user().getId();
        AccountResponseDto createdAccount = accountService.createAccount(createAccountDto, currentUserId);
        return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
    }
}
