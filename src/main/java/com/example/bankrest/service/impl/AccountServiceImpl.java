package com.example.bankrest.service.impl;

import com.example.bankrest.dto.AccountResponseDto;
import com.example.bankrest.dto.CreateAccountRequestDto;
import com.example.bankrest.entities.Account;
import com.example.bankrest.entities.User;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.dto.mapper.AccountMapper;
import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.repositories.UserRepository;
import com.example.bankrest.service.AccountGenerationService;
import com.example.bankrest.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountGenerationService accountGenerationService;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public AccountResponseDto createAccount(CreateAccountRequestDto createDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Account newAccount = Account.builder()
                .accountNumber(accountGenerationService.generateNewAccountNumber())
                .user(user)
                .balance(BigDecimal.ZERO)
                .currency(Currency.getInstance(createDto.getCurrency().toUpperCase()))
                .build();

        Account savedAccount = accountRepository.save(newAccount);
        log.info("IN createAccount - New account {} created for user {}", savedAccount.getAccountNumber(), user.getEmail());

        return accountMapper.toAccountResponseDto(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getUserAccounts(Long userId) {
        List<Account> accounts = accountRepository.findByUserId(userId);
        return accountMapper.toAccountResponseDtoList(accounts);
    }
}