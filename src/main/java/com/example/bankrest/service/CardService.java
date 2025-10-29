package com.example.bankrest.service;

import com.example.bankrest.dto.CardResponseDto;
import com.example.bankrest.dto.CreateCardRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.UUID;

public interface CardService {
    CardResponseDto createCard(CreateCardRequestDto createDto, Long userId);
    Page<CardResponseDto> getUserCards(Long userId, Pageable pageable);
    void blockCard(UUID cardUuid, UserDetails currentUserDetails);
    Page<CardResponseDto> getAllCards(Long userIdFilter, Pageable pageable);
    void deleteCard(UUID cardUuid);
}