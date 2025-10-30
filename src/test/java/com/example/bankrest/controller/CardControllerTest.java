package com.example.bankrest.controller;

import com.example.bankrest.dto.CardResponseDto;
import com.example.bankrest.dto.CreateCardRequestDto;
import com.example.bankrest.exception.GlobalExceptionHandler;
import com.example.bankrest.security.UserDetailsImpl;
import com.example.bankrest.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Card Controller Unit Tests (Standalone)")
class CardControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardController cardController;

    private Principal mockPrincipal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(cardController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        com.example.bankrest.entities.User mockUser = new com.example.bankrest.entities.User();
        mockUser.setId(1L);
        UserDetailsImpl userDetails = new UserDetailsImpl(mockUser);
        mockPrincipal = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("GET /v1/cards - should return 200 OK with paginated list")
    void getCurrentUserCards_ShouldReturnOk() throws Exception {
        // Arrange
        Pageable requestedPageable = PageRequest.of(0, 20);
        CardResponseDto mockDto = mock(CardResponseDto.class);
        Page<CardResponseDto> cardPage = new PageImpl<>(Collections.singletonList(mockDto), requestedPageable, 1);

        when(cardService.getUserCards(eq(1L), any(Pageable.class))).thenReturn(cardPage);

        // Act & Assert
        mockMvc.perform(get("/v1/cards")
                        .principal(mockPrincipal)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /v1/cards - should return 201 Created")
    void createCard_ShouldReturnCreated() throws Exception {
        CreateCardRequestDto requestDto = new CreateCardRequestDto();
        requestDto.setAccountUuid(UUID.randomUUID());
        requestDto.setCardProductId(1L);
        requestDto.setCardholderName("TEST USER");

        CardResponseDto responseDto = new CardResponseDto(UUID.randomUUID(), "**** 1234", null, null, null, null, null, null);
        when(cardService.createCard(any(CreateCardRequestDto.class), eq(1L)))
                .thenReturn(responseDto);

        mockMvc.perform(post("/v1/cards")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /v1/cards - should return 400 for invalid body")
    void createCard_invalidBody() throws Exception {
        CreateCardRequestDto invalidDto = new CreateCardRequestDto();

        mockMvc.perform(post("/v1/cards")
                        .principal(mockPrincipal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}