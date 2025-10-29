package com.example.bankrest.controller;

import com.example.bankrest.controller.api.CardApi;
import com.example.bankrest.dto.CardResponseDto;
import com.example.bankrest.dto.CreateCardRequestDto;
import com.example.bankrest.security.UserDetailsImpl;
import com.example.bankrest.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CardController implements CardApi {

    private final CardService cardService;

    @Override
    public ResponseEntity<Page<CardResponseDto>> getCurrentUserCards(Authentication authentication, Pageable pageable) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.user().getId();

        Page<CardResponseDto> cards = cardService.getUserCards(currentUserId, pageable);
        return ResponseEntity.ok(cards);
    }

    @Override
    public ResponseEntity<CardResponseDto> createCard(Authentication authentication, CreateCardRequestDto createCardRequestDto) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long currentUserId = userDetails.user().getId();
        CardResponseDto createdCard = cardService.createCard(createCardRequestDto, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCard);
    }

    @Override
    public ResponseEntity<Void> blockCard(Authentication authentication, UUID cardUuid) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        cardService.blockCard(cardUuid, userDetails);
        return ResponseEntity.noContent().build();
    }
}