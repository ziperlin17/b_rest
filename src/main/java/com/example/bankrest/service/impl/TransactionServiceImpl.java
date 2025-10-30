package com.example.bankrest.service.impl;

import com.example.bankrest.dto.TransferRequestDto;
import com.example.bankrest.entities.Account;
import com.example.bankrest.exception.BadRequestException;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.service.TransactionService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PessimisticLockScope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final EntityManager entityManager;
    private final TransactionHelper txHelper;

    @Override
    @Transactional
    public void transferFunds(TransferRequestDto transferDto, Long userId) {
        Account initialFromAccount = accountRepository.findByUuid(transferDto.getFromAccountUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Source account not found with UUID: " + transferDto.getFromAccountUuid()));
        if (!initialFromAccount.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own the source account.");
        }

        Account initialToAccount = accountRepository.findByAccountNumber(transferDto.getToAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Destination account not found with number: " + transferDto.getToAccountNumber()));
        if (initialFromAccount.getId().equals(initialToAccount.getId())) {
            throw new BadRequestException("Source and destination accounts cannot be the same.");
        }

        txHelper.performLockedTransfer(
                initialFromAccount.getId(),
                initialToAccount.getId(),
                transferDto.getAmount(),
                transferDto.getCurrency()
        );
    }
}

@Component
@RequiredArgsConstructor
@Slf4j
class TransactionHelper {

    private final EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void performLockedTransfer(Long fromAccountId, Long toAccountId, BigDecimal amount, String currency) {

        Account fromAccount;
        Account toAccount;

        if (fromAccountId < toAccountId) {
            fromAccount = entityManager.find(Account.class, fromAccountId, LockModeType.PESSIMISTIC_WRITE);
            toAccount = entityManager.find(Account.class, toAccountId, LockModeType.PESSIMISTIC_WRITE);
        } else {
            toAccount = entityManager.find(Account.class, toAccountId, LockModeType.PESSIMISTIC_WRITE);
            fromAccount = entityManager.find(Account.class, fromAccountId, LockModeType.PESSIMISTIC_WRITE);
        }

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient funds on the source account.");
        }
        if (!fromAccount.getCurrency().getCurrencyCode().equals(currency)) {
            throw new BadRequestException("Currency mismatch.");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        log.info("IN performLockedTransfer - Successfully transferred {} {} from account {} to account {}",
                amount, currency, fromAccount.getAccountNumber(), toAccount.getAccountNumber());
    }
}
