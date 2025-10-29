package com.example.bankrest.service.impl;

import com.example.bankrest.dto.AccountResponseDto;
import com.example.bankrest.dto.CreateAccountRequestDto;
import com.example.bankrest.entities.Account;
import com.example.bankrest.entities.User;
import com.example.bankrest.entities.enums.CardStatus;
import com.example.bankrest.exception.BadRequestException;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.dto.mapper.AccountMapper;
import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.repositories.CardRepository;
import com.example.bankrest.repositories.UserRepository;
import com.example.bankrest.service.AccountGenerationService;
import com.example.bankrest.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountGenerationService accountGenerationService;
    private final AccountMapper accountMapper;
    private final CardRepository cardRepository;

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

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getAccountByUuid(UUID uuid, Long userId) {
        Account account = accountRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with UUID: " + uuid));

        // Проверка прав
        if (!account.getUser().getId().equals(userId)) {
            log.warn("IN getAccountByUuid - User {} attempted to access account {} owned by {}", userId, uuid, account.getUser().getId());
            throw new AccessDeniedException("You do not have permission to view this account.");
        }

        return accountMapper.toAccountResponseDto(account);
    }

    @Override
    @Transactional
    public void closeAccount(UUID uuid, Long userId) {
        Account account = accountRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with UUID: " + uuid));

        if (!account.getUser().getId().equals(userId)) {
            log.warn("IN closeAccount - User {} attempted to close account {} owned by {}", userId, uuid, account.getUser().getId());
            throw new AccessDeniedException("You do not have permission to close this account.");
        }
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new BadRequestException("Account balance must be zero to close. Current balance: " + account.getBalance());
        }
        if (cardRepository.existsByAccountAndStatus(account, CardStatus.ACTIVE)) {
            throw new BadRequestException("Cannot close account. There are still ACTIVE cards linked to this account.");
        }

        accountRepository.delete(account);
        log.info("IN closeAccount - Account {} successfully closed by user {}", uuid, userId);
    }
}