package com.example.bankrest.dto.mapper;
import com.example.bankrest.dto.CardResponseDto;
import com.example.bankrest.entities.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = {AccountMapper.class})
public interface CardMapper {
    @Mapping(source = "uuid", target = "uuid")
    @Mapping(source = "cardholderName", target = "cardholderName")
    @Mapping(source = "expirationDate", target = "expirationDate")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "cardNumber", target = "maskedCardNumber", qualifiedByName = "maskCardNumber")
    @Mapping(source = "cardProduct.productName", target = "cardProductName")
    @Mapping(source = "account.uuid", target = "accountUuid")
    @Mapping(source = "account.balance", target = "accountBalance")
    CardResponseDto toCardResponseDto(Card card);

    List<CardResponseDto> toCardResponseDtoList(List<Card> cards);

    @Named("maskCardNumber")
    default String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() <= 4) {
            return "**** **** **** ****";
        }
        String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFourDigits;
    }
}
