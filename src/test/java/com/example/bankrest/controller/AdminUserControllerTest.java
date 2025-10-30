package com.example.bankrest.controller;

import com.example.bankrest.dto.UserResponseDto;
import com.example.bankrest.dto.UserStatusUpdateRequestDto;
import com.example.bankrest.entities.enums.UserStatus;
import com.example.bankrest.exception.GlobalExceptionHandler;
import com.example.bankrest.exception.ResourceNotFoundException;
import com.example.bankrest.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.List;
import java.util.Set;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Admin User Controller Unit Tests")
class AdminUserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminUserController adminUserController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(adminUserController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /v1/admin/users Endpoint")
    class GetAllUsersTests {

        @Test
        @DisplayName("should return 200 OK with a paginated list of users")
        void getAllUsers_shouldReturnPaginatedUsers() throws Exception {
            // Arrange
            UserResponseDto userDto = new UserResponseDto();
            userDto.setId(1L);
            userDto.setEmail("test.user@example.com");
            userDto.setStatus(UserStatus.ACTIVE);
            userDto.setRoles(Set.of("ROLE_USER"));

            Pageable pageable = PageRequest.of(0, 10);
            Page<UserResponseDto> userPage = new PageImpl<>(List.of(userDto), pageable, 1);

            when(userService.getAllUsers(any(Pageable.class))).thenReturn(userPage);

            mockMvc.perform(get("/v1/admin/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content.length()", is(1)))
                    .andExpect(jsonPath("$.content[0].email", is("test.user@example.com")))
                    .andExpect(jsonPath("$.totalElements", is(1)));
        }

        @Test
        @DisplayName("should pass pagination and sort parameters to the service layer")
        void getAllUsers_shouldUsePaginationAndSortParameters() throws Exception {
            Pageable expectedPageable = PageRequest.of(1, 5, Sort.by("email").descending());
            when(userService.getAllUsers(expectedPageable)).thenReturn(Page.empty(expectedPageable));

            mockMvc.perform(get("/v1/admin/users?page=1&size=5&sort=email,desc"))
                    .andExpect(status().isOk());

            verify(userService).getAllUsers(expectedPageable);
        }
    }

    @Nested
    @DisplayName("PATCH /v1/admin/users/{id}/status Endpoint")
    class UpdateUserStatusTests {

        @Test
        @DisplayName("should return 200 OK with the updated user DTO on success")
        void updateUserStatus_whenSuccessful_shouldReturnUpdatedUser() throws Exception {
            long userIdToUpdate = 1L;
            UserStatusUpdateRequestDto updateRequest = new UserStatusUpdateRequestDto();
            updateRequest.setNewStatus(UserStatus.SUSPENDED);

            UserResponseDto expectedResponse = new UserResponseDto();
            expectedResponse.setId(userIdToUpdate);
            expectedResponse.setStatus(UserStatus.SUSPENDED);
            expectedResponse.setEmail("user.to.suspend@example.com");

            when(userService.updateUserStatus(eq(userIdToUpdate), eq(UserStatus.SUSPENDED)))
                    .thenReturn(expectedResponse);

            mockMvc.perform(patch("/v1/admin/users/{id}/status", userIdToUpdate)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is((int) userIdToUpdate)))
                    .andExpect(jsonPath("$.status", is("SUSPENDED")));
        }

        @Test
        @DisplayName("should return 404 Not Found if user does not exist")
        void updateUserStatus_whenUserNotFound_shouldReturnNotFound() throws Exception {
            long nonExistentUserId = 999L;
            UserStatusUpdateRequestDto updateRequest = new UserStatusUpdateRequestDto();
            updateRequest.setNewStatus(UserStatus.ACTIVE);

            String expectedErrorMessage = "User not found with id: " + nonExistentUserId;
            when(userService.updateUserStatus(eq(nonExistentUserId), any(UserStatus.class)))
                    .thenThrow(new ResourceNotFoundException(expectedErrorMessage));

            mockMvc.perform(patch("/v1/admin/users/{id}/status", nonExistentUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status", is(404)))
                    .andExpect(jsonPath("$.message", is(expectedErrorMessage)));
        }

        @Test
        @DisplayName("should return 400 Bad Request if request body has validation errors")
        void updateUserStatus_whenRequestBodyIsInvalid_shouldReturnBadRequest() throws Exception {
            String invalidJson = "{}";

            mockMvc.perform(patch("/v1/admin/users/{id}/status", 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());
        }
    }
}
