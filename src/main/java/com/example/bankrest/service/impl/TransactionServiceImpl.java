package com.example.bankrest.service.impl;

import com.example.bankrest.dto.TransferRequestDto;
import com.example.bankrest.entities.Account;
import com.example.bankrest.exception.BadRequestException;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void transferFunds(TransferRequestDto transferDto, Long userId) {
        Account fromAccount = accountRepository.findWithLockingByUuid(transferDto.getFromAccountUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found with UUID: " + transferDto.getFromAccountUuid()));

        if (!fromAccount.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own the source account.");
        }

        Account toAccount = accountRepository.findByAccountNumber(transferDto.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found with number: " + transferDto.getToAccountNumber()));

        if (fromAccount.getId().equals(toAccount.getId())) {
            throw new BadRequestException("Source and destination accounts cannot be the same.");
        }

        if (fromAccount.getBalance().compareTo(transferDto.getAmount()) < 0) {
            throw new BadRequestException("Insufficient funds on the source account.");
        }

        if (!fromAccount.getCurrency().getCurrencyCode().equals(transferDto.getCurrency())) {
            throw new BadRequestException("Currency mismatch.");
        }

        BigDecimal fromNewBalance = fromAccount.getBalance().subtract(transferDto.getAmount());
        BigDecimal toNewBalance = toAccount.getBalance().add(transferDto.getAmount());

        fromAccount.setBalance(fromNewBalance);
        toAccount.setBalance(toNewBalance);

        log.info("IN transferFunds - Successfully transferred {} {} from account {} to account {}",
                transferDto.getAmount(), transferDto.getCurrency(), fromAccount.getAccountNumber(), toAccount.getAccountNumber());

    }
}
