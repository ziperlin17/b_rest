package com.example.bankrest.controller;

import com.example.bankrest.controller.api.AdminCardApi;
import com.example.bankrest.dto.CardResponseDto;
import com.example.bankrest.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AdminCardController implements AdminCardApi {

    private final CardService cardService;

    @Override
    public ResponseEntity<Page<CardResponseDto>> getAllCards(Long userId, Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(userId, pageable));
    }

    @Override
    public ResponseEntity<Void> deleteCard(UUID cardUuid) {
        cardService.deleteCard(cardUuid);
        return ResponseEntity.noContent().build();
    }
}
