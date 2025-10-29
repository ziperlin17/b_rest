package com.example.bankrest.controller.api;

import com.example.bankrest.dto.CardResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

@Tag(name = "Мои карты", description = "Операции для управления личными картами")
public interface UserCardApi {

    @Operation(
            summary = "Получить список моих карт",
            description = "Возвращает все банковские карты, привязанные к вашему аккаунту. " +
                    "Результат возвращается постранично (по умолчанию 10 карт на страницу). " +
                    "Номера карт в ответе замаскированы"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешно. Возвращает список ваших карт.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Вы не авторизованы. Пожалуйста, войдите в систему и передайте токен.",
                    content = @Content
            ),
    })
    ResponseEntity<Page<CardResponseDto>> getMyCards(
            @Parameter(hidden = true) UserDetails userDetails,
            @Parameter(description = "Настройки пагинации (номер страницы, количество элементов, сортировка)") Pageable pageable
    );
}