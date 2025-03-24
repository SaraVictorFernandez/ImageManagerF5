package com.f5.tech_test.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.f5.tech_test.dto.LoginRequest;
import com.f5.tech_test.dto.LoginResponse;
import com.f5.tech_test.dto.RegisterRequest;
import com.f5.tech_test.dto.UserDTO;
import com.f5.tech_test.entities.User;
import com.f5.tech_test.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_WithValidData_ShouldCreateUser() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andReturn();

        // Verify user was created in database
        User savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        // Arrange
        // Create a test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");

        // Act & Assert
        MvcResult result = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Verify token
        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        assertNotNull(loginResponse.getToken());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturnForbidden() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("wronguser");
        request.setPassword("wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void register_WithExistingUsername_ShouldReturnConflict() throws Exception {
        // Arrange
        // Create a test user first
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        // Try to register with same username
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("another@example.com");
        request.setPassword("password123");

        // Act & Assert
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void changePassword_WithValidToken_ShouldUpdatePasswordAndReturnNewToken() throws Exception {
        // Arrange
        // Create and login a test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponseObj = objectMapper.readValue(loginResponse, LoginResponse.class);
        String token = loginResponseObj.getToken();

        // Change password
        String newPassword = "newpassword123";
        MvcResult changePasswordResult = mockMvc.perform(post("/api/users/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"" + newPassword + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        // Verify new token works
        String newToken = objectMapper.readValue(changePasswordResult.getResponse().getContentAsString(), LoginResponse.class).getToken();
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("testuser", newPassword))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());

        // Verify old token no longer works
        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new LoginRequest("testuser", "password123"))))
                .andExpect(status().isForbidden());

        // Verify password was updated in database
        User updatedUser = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
    }

    @Test
    void changePassword_WithInvalidToken_ShouldReturnForbidden() throws Exception {
        // Arrange
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImlhdCI6MTcwOTc5MjgwMCwiZXhwIjoxNzA5Nzk2NDAwfQ.invalid_signature";

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"newpassword123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void changePassword_WithEmptyPassword_ShouldReturnBadRequest() throws Exception {
        // Arrange
        // Create and login a test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String loginResponse = loginResult.getResponse().getContentAsString();
        LoginResponse loginResponseObj = objectMapper.readValue(loginResponse, LoginResponse.class);
        String token = loginResponseObj.getToken();

        // Act & Assert
        mockMvc.perform(post("/api/users/change-password")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
} 