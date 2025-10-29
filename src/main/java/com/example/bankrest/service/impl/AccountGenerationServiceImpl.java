package com.example.bankrest.service.impl;

import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.service.AccountGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountGenerationServiceImpl implements AccountGenerationService {

    private static final String ACCOUNT_PREFIX = "ACC";
    private static final int RANDOM_PART_LENGTH = 10;

    private final AccountRepository accountRepository;
    private final Random random = new SecureRandom();

    @Override
    public String generateNewAccountNumber() {
        String accountNumber;
        do {
            accountNumber = generate();
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        return accountNumber;
    }

    private String generate() {
        StringBuilder number = new StringBuilder(ACCOUNT_PREFIX);
        for (int i = 0; i < RANDOM_PART_LENGTH; i++) {
            number.append(random.nextInt(10));
        }
        return number.toString();
    }
}