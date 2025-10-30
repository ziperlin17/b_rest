package com.example.bankrest.service.impl;

import com.example.bankrest.entities.Account;
import com.example.bankrest.entities.CardProduct;
import com.example.bankrest.entities.User;
import com.example.bankrest.entities.enums.UserStatus;
import com.example.bankrest.exception.BadRequestException;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.repositories.CardProductRepository;
import com.example.bankrest.repositories.CardRepository;
import com.example.bankrest.repositories.UserRepository;
import com.example.bankrest.service.AccountService;
import com.example.bankrest.service.CardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.example.bankrest.dto.CreateCardRequestDto;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AccountServiceImplIT {

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
    private AccountService accountService;
    @Autowired
    private CardService cardService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardRepository cardRepository;
    @Autowired
    private CardProductRepository cardProductRepository;

    private User testUser;
    private Account accountToClose;
    private CardProduct testCardProduct;

    @BeforeEach
    void setUp() {
        testUser = User.builder().email("user.close@test.com").password("pwd").phoneNumber("333").firstName("Test").lastName("User").status(UserStatus.ACTIVE).build();
        userRepository.save(testUser);

        accountToClose = Account.builder().uuid(UUID.randomUUID()).user(testUser).accountNumber("ACC_CLOSE").currency(Currency.getInstance("USD")).balance(BigDecimal.ZERO).build();
        accountRepository.save(accountToClose);

        testCardProduct = CardProduct.builder().productName("Test Card").paymentSystem("VISA").build();
        cardProductRepository.save(testCardProduct);
    }

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
        accountRepository.deleteAll();
        cardProductRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("closeAccount: Race conditions when closing an account and creating a card at the same time should be handled correctly")
    void closeAccount_shouldHandleRaceConditionWithCardCreation() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(2);
        final CountDownLatch latch = new CountDownLatch(1);
        final UUID accountUuid = accountToClose.getUuid();
        final Long userId = testUser.getId();

        Future<Exception> closeAccountResult = executor.submit(() -> {
            try {
                latch.await();
                accountService.closeAccount(accountUuid, userId);
                return null;
            } catch (Exception e) {
                return e;
            }
        });

        Future<Exception> createCardResult = executor.submit(() -> {
            try {
                latch.await();
                CreateCardRequestDto dto = new CreateCardRequestDto();
                dto.setAccountUuid(accountUuid);
                dto.setCardProductId(testCardProduct.getId());
                dto.setCardholderName("TEST USER");
                cardService.createCard(dto, userId);
                return null;
            } catch (Exception e) {
                return e;
            }
        });

        latch.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(15, TimeUnit.SECONDS), "Test timeout");

        Exception closeException = closeAccountResult.get();
        Exception createException = createCardResult.get();

        assertTrue(
                (closeException == null && createException != null) || (closeException != null && createException == null),
                "Either a successful close and a failed create were expected, or vice versa. But both were either successful or failed"
        );

        if (closeException == null) {
            assertNotNull(createException.getCause(), "failed create operation should have cause)");
            assertFalse(accountRepository.findByUuid(accountUuid).isPresent(), "account should be deleted");
            assertEquals(0, cardRepository.count(), "0 cards should be created");
        } else {
            System.out.println("Card created.");
            assertNotNull(closeException.getCause(), "failed close operation should have cause");
            assertTrue(accountRepository.findByUuid(accountUuid).isPresent(), "account should not be deleted");
            assertEquals(1, cardRepository.count(), "should be created 1 card");
        }
    }

}