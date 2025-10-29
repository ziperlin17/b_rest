package com.example.bankrest.service.impl;

import com.example.bankrest.dto.CardResponseDto;
import com.example.bankrest.dto.CreateCardRequestDto;
import com.example.bankrest.entities.enums.RoleType;
import com.example.bankrest.security.UserDetailsImpl;
import com.example.bankrest.util.GeneratedCardDetails;
import com.example.bankrest.entities.Account;
import com.example.bankrest.entities.Card;
import com.example.bankrest.entities.CardProduct;
import com.example.bankrest.entities.enums.CardStatus;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.dto.mapper.CardMapper;
import com.example.bankrest.repositories.AccountRepository;
import com.example.bankrest.repositories.CardProductRepository;
import com.example.bankrest.repositories.CardRepository;
import com.example.bankrest.service.CardGenerationService;
import com.example.bankrest.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.GrantedAuthority;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final CardProductRepository cardProductRepository;
    private final CardGenerationService cardGenerationService;
    private final CardMapper cardMapper;

    @Override
    @Transactional
    public CardResponseDto createCard(CreateCardRequestDto createDto, Long userId) {
        Account account = accountRepository.findByUuid(createDto.getAccountUuid())
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with UUID: " + createDto.getAccountUuid()));
        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to create a card for this account.");
        }
        CardProduct cardProduct = cardProductRepository.findById(createDto.getCardProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Card product not found with ID: " + createDto.getCardProductId()));

        GeneratedCardDetails details = cardGenerationService.generateNewCardDetails();

        Card newCard = Card.builder()
                .cardholderName(createDto.getCardholderName().toUpperCase())
                .cardNumber(details.cardNumber())
                .cvv(details.cvv())
                .expirationDate(details.expirationDate())
                .status(CardStatus.ACTIVE)
                .account(account)
                .cardProduct(cardProduct)
                .build();

        Card savedCard = cardRepository.save(newCard);
        log.info("IN createCard - New card with UUID {} created for user {}", savedCard.getUuid(), userId);

        return cardMapper.toCardResponseDto(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> getUserCards(Long userId, Pageable pageable) {
        Page<Card> cards = cardRepository.findByAccount_User_Id(userId, pageable);
        return cards.map(cardMapper::toCardResponseDto);
    }

    @Override
    @Transactional
    public void blockCard(UUID cardUuid, UserDetails currentUserDetails) {
        Card card = cardRepository.findByUuid(cardUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with UUID: " + cardUuid));

        Long currentUserId = ((UserDetailsImpl) currentUserDetails).user().getId();

        boolean isAdmin = currentUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals(RoleType.ROLE_ADMIN.name()));

        if (!isAdmin && !card.getAccount().getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You do not have permission to block this card.");
        }

        if (card.getStatus() == CardStatus.BLOCKED) {
            log.warn("IN blockCard - Card {} is already blocked", cardUuid);
            return;
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        if (isAdmin) {
            log.info("IN blockCard - Card with UUID {} was blocked by ADMIN {}", cardUuid, currentUserDetails.getUsername());
        } else {
            log.info("IN blockCard - Card with UUID {} was blocked by user {}", cardUuid, currentUserId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponseDto> getAllCards(Long userIdFilter, Pageable pageable) {
        Page<Card> cards;
        if (userIdFilter != null) {
            cards = cardRepository.findByAccount_User_Id(userIdFilter, pageable);
        } else {
            cards = cardRepository.findAll(pageable);
        }
        return cards.map(cardMapper::toCardResponseDto);
    }

    @Override
    @Transactional
    public void deleteCard(UUID cardUuid) {
        Card card = cardRepository.findByUuid(cardUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found with UUID: " + cardUuid));

        cardRepository.delete(card);
        log.info("IN deleteCard - Card with UUID {} was deleted by an admin", cardUuid);
    }
}

