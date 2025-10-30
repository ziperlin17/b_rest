package com.example.bankrest.service.impl;

import com.example.bankrest.dto.AccountResponseDto;
import com.example.bankrest.dto.CreateAccountRequestDto;
import com.example.bankrest.dto.mapper.AccountMapper;
import com.example.bankrest.entities.Account;
import com.example.bankrest.entities.User;
import com.example.bankrest.entities.enums.CardStatus;
import com.example.bankrest.exception.BadRequestException;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.repositories.CardRepository;
import com.example.bankrest.repositories.UserRepository;
import com.example.bankrest.service.AccountGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountGenerationService accountGenerationService;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private User testUser;
    private Account testAccount;
    private UUID accountUuid;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        accountUuid = UUID.randomUUID();

        testAccount = new Account();
        testAccount.setUuid(accountUuid);
        testAccount.setUser(testUser);
        testAccount.setBalance(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("createAccount: successful account creation")
    void createAccount_ShouldCreateAccountSuccessfully() {
        CreateAccountRequestDto createDto = new CreateAccountRequestDto();
        createDto.setCurrency("USD");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountGenerationService.generateNewAccountNumber()).thenReturn("TEST_ACC_NUM");
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(accountMapper.toAccountResponseDto(any(Account.class))).thenReturn(new AccountResponseDto(accountUuid, "TEST_ACC_NUM", BigDecimal.valueOf(1000), Currency.getInstance("USD")));

        AccountResponseDto result = accountService.createAccount(createDto, 1L);

        assertNotNull(result);
        assertEquals("TEST_ACC_NUM", result.accountNumber());

        verify(userRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    @DisplayName("closeAccount: successful account closing")
    void closeAccount_ShouldCloseAccountSuccessfully() {
        when(accountRepository.findByUuid(accountUuid)).thenReturn(Optional.of(testAccount));
        when(cardRepository.existsByAccountAndStatus(testAccount, CardStatus.ACTIVE)).thenReturn(false);
        assertDoesNotThrow(() -> accountService.closeAccount(accountUuid, 1L));
        verify(accountRepository, times(1)).delete(testAccount);
    }

    @Test
    @DisplayName("closeAccount: zero balance closing error")
    void closeAccount_ShouldThrowException_WhenBalanceIsNotZero() {
        testAccount.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findByUuid(accountUuid)).thenReturn(Optional.of(testAccount));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> accountService.closeAccount(accountUuid, 1L));

        assertTrue(exception.getMessage().contains("Account balance must be zero to close."));

        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    @DisplayName("closeAccount: active cards account closing error")
    void closeAccount_ShouldThrowException_WhenActiveCardsExist() {
        when(accountRepository.findByUuid(accountUuid)).thenReturn(Optional.of(testAccount));
        when(cardRepository.existsByAccountAndStatus(testAccount, CardStatus.ACTIVE)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> accountService.closeAccount(accountUuid, 1L));

        assertEquals("Cannot close account. There are still ACTIVE cards linked to this account.", exception.getMessage());
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    @DisplayName("closeAccount: wrong account closing error")
    void closeAccount_ShouldThrowException_WhenUserDoesNotOwnAccount() {
        Long otherUserId = 2L;
        when(accountRepository.findByUuid(accountUuid)).thenReturn(Optional.of(testAccount));

        assertThrows(AccessDeniedException.class,
                () -> accountService.closeAccount(accountUuid, otherUserId));

        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    @DisplayName("closeAccount: account not found")
    void closeAccount_ShouldThrowException_WhenAccountNotFound() {
        when(accountRepository.findByUuid(accountUuid)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> accountService.closeAccount(accountUuid, 1L));
    }
}