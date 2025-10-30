package com.example.bankrest.service.impl;

import com.example.bankrest.dto.TransferRequestDto;
import com.example.bankrest.entities.Account;
import com.example.bankrest.entities.User;
import com.example.bankrest.exception.BadRequestException;
import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.repositories.UserRepository;
import com.example.bankrest.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.example.bankrest.entities.enums.UserStatus;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TransactionServiceImplIT {
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("app.encryption.aes.key", () -> "12345678901234567890123456789012");
        registry.add("app.security.jwt.secret", () -> "test-jwt-secret-key-that-is-long-enough-for-hs256");

    }

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private User userA;
    private User userB;
    private Account accountA;
    private Account accountB;

    @BeforeEach
    void setUp() {
        userA = User.builder().email("user.a@test.com").password("pwd").phoneNumber("111").firstName("A").lastName("User").status(UserStatus.ACTIVE).build();
        userB = User.builder().email("user.b@test.com").password("pwd").phoneNumber("222").firstName("B").lastName("User").status(UserStatus.ACTIVE).build();
        userA = userRepository.save(userA);
        userB = userRepository.save(userB);

        accountA = Account.builder().uuid(UUID.randomUUID()).user(userA).accountNumber("ACC_A").currency(Currency.getInstance("USD")).balance(new BigDecimal("1000.00")).build();
        accountB = Account.builder().uuid(UUID.randomUUID()).user(userB).accountNumber("ACC_B").currency(Currency.getInstance("USD")).balance(new BigDecimal("500.00")).build();
        accountA = accountRepository.save(accountA);
        accountB = accountRepository.save(accountB);
    }

    @AfterEach
    void tearDown() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Successful transfer of funds between accounts")
    void shouldTransferFundsSuccessfully() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setFromAccountUuid(accountA.getUuid());
        transferDto.setToAccountNumber(accountB.getAccountNumber());
        transferDto.setAmount(new BigDecimal("100.00"));
        transferDto.setCurrency("USD");
        transactionService.transferFunds(transferDto, userA.getId());

        Account updatedAccountA = accountRepository.findById(accountA.getId()).get();
        Account updatedAccountB = accountRepository.findById(accountB.getId()).get();

        assertEquals(0, new BigDecimal("900.00").compareTo(updatedAccountA.getBalance()), "Баланс отправителя неверен");
        assertEquals(0, new BigDecimal("600.00").compareTo(updatedAccountB.getBalance()), "Баланс получателя неверен");
    }

    @Test
    @DisplayName("Transfer error: insufficient funds")
    void shouldFailOnInsufficientFunds() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setFromAccountUuid(accountA.getUuid());
        transferDto.setToAccountNumber(accountB.getAccountNumber());
        transferDto.setAmount(new BigDecimal("2000.00"));
        transferDto.setCurrency("USD");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            transactionService.transferFunds(transferDto, userA.getId());
        });

        assertEquals("Insufficient funds on the source account.", exception.getMessage());
    }

    @Test
    @DisplayName("Transfer error: user does not own the source account")
    void shouldFailWhenUserDoesNotOwnSourceAccount() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setFromAccountUuid(accountA.getUuid()); // Счет A
        transferDto.setToAccountNumber(accountB.getAccountNumber());
        transferDto.setAmount(new BigDecimal("100.00"));
        transferDto.setCurrency("USD");

        assertThrows(AccessDeniedException.class, () -> {
            transactionService.transferFunds(transferDto, userB.getId());
        });
    }

    @Test
    @DisplayName("Transfer error: transfer to the same account")
    void shouldFailOnSelfTransfer() {
        TransferRequestDto transferDto = new TransferRequestDto();
        transferDto.setFromAccountUuid(accountA.getUuid());
        transferDto.setToAccountNumber(accountA.getAccountNumber());
        transferDto.setAmount(new BigDecimal("50.00"));
        transferDto.setCurrency("USD");

        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            transactionService.transferFunds(transferDto, userA.getId());
        });

        assertEquals("Source and destination accounts cannot be the same.", exception.getMessage());
    }

    @Test
    @DisplayName("Correct processing of simultaneous counter transfers without deadlock")
    void shouldHandleConcurrentTransfersWithoutDeadlock() throws InterruptedException {
        final int concurrentRequests = 2;
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch finishLatch = new CountDownLatch(concurrentRequests);
        final ExecutorService executorService = Executors.newFixedThreadPool(concurrentRequests);

        executorService.submit(() -> {
            try {
                startLatch.await();
                TransferRequestDto dto = new TransferRequestDto();
                dto.setFromAccountUuid(accountA.getUuid());
                dto.setToAccountNumber(accountB.getAccountNumber());
                dto.setAmount(new BigDecimal("100.00"));
                dto.setCurrency("USD");
                transactionService.transferFunds(dto, userA.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                startLatch.await();
                TransferRequestDto dto = new TransferRequestDto();
                dto.setFromAccountUuid(accountB.getUuid());
                dto.setToAccountNumber(accountA.getAccountNumber());
                dto.setAmount(new BigDecimal("50.00"));
                dto.setCurrency("USD");
                transactionService.transferFunds(dto, userB.getId());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishLatch.countDown();
            }
        });

        startLatch.countDown();
        boolean finishedInTime = finishLatch.await(10, TimeUnit.SECONDS);

        assertTrue(finishedInTime, "Test not passed. may be deadlock");

        Account finalAccountA = accountRepository.findById(accountA.getId()).get();
        Account finalAccountB = accountRepository.findById(accountB.getId()).get();

        assertThat(finalAccountA.getBalance()).isEqualByComparingTo("950.00");
        assertThat(finalAccountB.getBalance()).isEqualByComparingTo("550.00");
    }
}