package com.f5.tech_test.controllers;

import com.f5.tech_test.dto.UserDTO;
import com.f5.tech_test.dto.RegisterRequest;
import com.f5.tech_test.entities.User;
import com.f5.tech_test.exceptions.UserAlreadyExistsException;
import com.f5.tech_test.exceptions.UserNotFoundException;
import com.f5.tech_test.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private User testUser;
    private UserDTO testUserDTO;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setCreatedAt(NOW);
        testUser.setLastLogin(NOW);

        testUserDTO = new UserDTO();
        testUserDTO.setId(1L);
        testUserDTO.setUsername("testuser");
        testUserDTO.setEmail("test@example.com");
    }


    @Test
    void getAllUsers_ShouldReturnListOfUserDTOs() throws Exception {
        // Arrange
        List<UserDTO> users = Arrays.asList(testUserDTO);
        when(userService.getAllUsers()).thenReturn(users);

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testUserDTO.getId()))
                .andExpect(jsonPath("$[0].username").value(testUserDTO.getUsername()))
                .andExpect(jsonPath("$[0].email").value(testUserDTO.getEmail()));
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUserDTO() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserDTO.getId()))
                .andExpect(jsonPath("$.username").value(testUserDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(testUserDTO.getEmail()));
    }

    @Test
    void getUserById_WithNonExistingUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).getUserById(1L);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void getUserByUsername_WithExistingUser_ShouldReturnUserDTO() throws Exception {
        // Arrange
        when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserDTO.getId()))
                .andExpect(jsonPath("$.username").value(testUserDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(testUserDTO.getEmail()));
    }

    @Test
    void getUserByUsername_WithNonExistingUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).getUserByUsername("nonexistent");

        // Act & Assert
        mockMvc.perform(get("/api/users/username/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void updateUser_WithExistingUser_ShouldReturnUpdatedUserDTO() throws Exception {
        // Arrange
        when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(testUserDTO);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUserDTO.getId()))
                .andExpect(jsonPath("$.username").value(testUserDTO.getUsername()))
                .andExpect(jsonPath("$.email").value(testUserDTO.getEmail()));
    }

    @Test
    void updateUser_WithNonExistingUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).updateUser(eq(1L), any(UserDTO.class));

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"testuser\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void deleteUser_WithExistingUser_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_WithNonExistingUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException("User not found"))
                .when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }
} 