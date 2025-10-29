package com.example.bankrest.dto.mapper;

import com.example.bankrest.dto.AccountResponseDto;
import com.example.bankrest.entities.Account;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    AccountResponseDto toAccountResponseDto(Account account);
    List<AccountResponseDto> toAccountResponseDtoList(List<Account> accounts);
}