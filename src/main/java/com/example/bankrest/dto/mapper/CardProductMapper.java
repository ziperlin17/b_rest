package com.example.bankrest.dto.mapper;

import com.example.bankrest.dto.CardProductResponseDto;
import com.example.bankrest.dto.CreateCardProductRequestDto;
import com.example.bankrest.entities.CardProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardProductMapper {

    CardProductResponseDto toDto(CardProduct cardProduct);

    List<CardProductResponseDto> toDtoList(List<CardProduct> cardProducts);

    @Mapping(target = "id", ignore = true)
    CardProduct toEntity(CreateCardProductRequestDto createDto);
}
