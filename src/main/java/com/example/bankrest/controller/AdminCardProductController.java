package com.example.bankrest.controller;

import com.example.bankrest.controller.api.AdminCardProductApi;
import com.example.bankrest.dto.CardProductResponseDto;
import com.example.bankrest.dto.CreateCardProductRequestDto;
import com.example.bankrest.dto.UpdateCardProductRequestDto;
import com.example.bankrest.service.CardProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminCardProductController implements AdminCardProductApi {

    private final CardProductService cardProductService;

    @Override
    public ResponseEntity<List<CardProductResponseDto>> getAllCardProducts() {
        return ResponseEntity.ok(cardProductService.getAllCardProducts());
    }

    @Override
    public ResponseEntity<CardProductResponseDto> createCardProduct(CreateCardProductRequestDto createDto) {
        CardProductResponseDto createdProduct = cardProductService.createCardProduct(createDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<CardProductResponseDto> updateCardProduct(Long id, UpdateCardProductRequestDto updateDto) {
        return ResponseEntity.ok(cardProductService.updateCardProduct(id, updateDto));
    }
}