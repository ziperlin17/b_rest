package com.example.bankrest.service.impl;

import com.example.bankrest.dto.CardProductResponseDto;
import com.example.bankrest.dto.CreateCardProductRequestDto;
import com.example.bankrest.dto.UpdateCardProductRequestDto;
import com.example.bankrest.dto.mapper.CardProductMapper;
import com.example.bankrest.entities.CardProduct;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.repositories.CardProductRepository;
import com.example.bankrest.service.CardProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardProductServiceImpl implements CardProductService {

    private final CardProductRepository cardProductRepository;
    private final CardProductMapper cardProductMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CardProductResponseDto> getAllCardProducts() {
        List<CardProduct> products = cardProductRepository.findAll();
        return cardProductMapper.toDtoList(products);
    }

    @Override
    @Transactional
    public CardProductResponseDto createCardProduct(CreateCardProductRequestDto createDto) {
        CardProduct cardProduct = cardProductMapper.toEntity(createDto);
        CardProduct savedProduct = cardProductRepository.save(cardProduct);
        log.info("IN createCardProduct - new product created with id {}", savedProduct.getId());
        return cardProductMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    public CardProductResponseDto updateCardProduct(Long productId, UpdateCardProductRequestDto updateDto) {
        CardProduct product = cardProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("CardProduct not found with id: " + productId));

        Optional.ofNullable(updateDto.getProductName()).ifPresent(product::setProductName);
        Optional.ofNullable(updateDto.getPaymentSystem()).ifPresent(product::setPaymentSystem);

        CardProduct updatedProduct = cardProductRepository.save(product);
        log.info("IN updateCardProduct - product with id {} updated", updatedProduct.getId());
        return cardProductMapper.toDto(updatedProduct);
    }
}