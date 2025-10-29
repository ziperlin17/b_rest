package com.example.bankrest.service;

import com.example.bankrest.dto.CardProductResponseDto;
import com.example.bankrest.dto.CreateCardProductRequestDto;
import com.example.bankrest.dto.UpdateCardProductRequestDto;
import java.util.List;

public interface CardProductService {
    List<CardProductResponseDto> getAllCardProducts();
    CardProductResponseDto createCardProduct(CreateCardProductRequestDto createDto);
    CardProductResponseDto updateCardProduct(Long productId, UpdateCardProductRequestDto updateDto);
}