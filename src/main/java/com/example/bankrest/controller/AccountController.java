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
import java.util.UUID;

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

    @Override
    public ResponseEntity<AccountResponseDto> getAccountByUuid(Authentication authentication, UUID uuid) {
        Long currentUserId = ((UserDetailsImpl) authentication.getPrincipal()).user().getId();
        AccountResponseDto account = accountService.getAccountByUuid(uuid, currentUserId);
        return ResponseEntity.ok(account);
    }

    @Override
    public ResponseEntity<Void> closeAccount(Authentication authentication, UUID uuid) {
        Long currentUserId = ((UserDetailsImpl) authentication.getPrincipal()).user().getId();
        accountService.closeAccount(uuid, currentUserId);
        return ResponseEntity.noContent().build();
    }
}
