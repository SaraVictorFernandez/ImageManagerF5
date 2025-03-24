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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser1;
    private User testUser2;
    private String testUser1Token;
    private String testUser2Token;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // Create test users
        testUser1 = new User();
        testUser1.setUsername("testuser1");
        testUser1.setEmail("test1@example.com");
        testUser1.setPassword(passwordEncoder.encode("password123"));
        testUser1 = userRepository.save(testUser1);

        testUser2 = new User();
        testUser2.setUsername("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setPassword(passwordEncoder.encode("password123"));
        testUser2 = userRepository.save(testUser2);

        // Get tokens for both users
        testUser1Token = loginAndGetToken("testuser1", "password123");
        testUser2Token = loginAndGetToken("testuser2", "password123");
    }

    private String loginAndGetToken(String username, String password) throws Exception {
        LoginRequest loginRequest = new LoginRequest(username, password);
        MvcResult result = mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        LoginResponse loginResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                LoginResponse.class
        );
        return loginResponse.getToken();
    }

    @Test
    void getUserById_WithOwnId_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser1.getId())
                .header("Authorization", "Bearer " + testUser1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUser1.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser1.getEmail()));
    }

    @Test
    void getUserById_WithOtherUserId_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/{id}", testUser2.getId())
                .header("Authorization", "Bearer " + testUser1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUser2.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser2.getEmail()));
    }

    @Test
    void getUserByUsername_WithOwnUsername_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/username/{username}", testUser1.getUsername())
                .header("Authorization", "Bearer " + testUser1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUser1.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser1.getEmail()));
    }

    @Test
    void getUserByUsername_WithOtherUsername_ShouldReturnUser() throws Exception {
        mockMvc.perform(get("/api/users/username/{username}", testUser2.getUsername())
                .header("Authorization", "Bearer " + testUser1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(testUser2.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser2.getEmail()));
    }

    @Test
    void updateUser_WithOwnId_ShouldUpdateUser() throws Exception {
        UserDTO updateRequest = new UserDTO();
        updateRequest.setUsername(testUser1.getUsername());
        updateRequest.setEmail("updated@example.com");

        mockMvc.perform(put("/api/users/{id}", testUser1.getId())
                .header("Authorization", "Bearer " + testUser1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"));
    }

    @Test
    void updateUser_WithOtherUserId_ShouldReturnForbidden() throws Exception {
        UserDTO updateRequest = new UserDTO();
        updateRequest.setUsername(testUser2.getUsername());
        updateRequest.setEmail("updated@example.com");

        mockMvc.perform(put("/api/users/{id}", testUser2.getId())
                .header("Authorization", "Bearer " + testUser1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_WithOwnId_ShouldDeleteUser() throws Exception {
        // First verify the user exists
        mockMvc.perform(get("/api/users/{id}", testUser1.getId()))
                .andExpect(status().isOk());

        // Delete the user
        mockMvc.perform(delete("/api/users/{id}", testUser1.getId())
                .header("Authorization", "Bearer " + testUser1Token))
                .andExpect(status().isNoContent());

        // Try to access the deleted user with the same token (should fail with 403)
        mockMvc.perform(get("/api/users/{id}", testUser1.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_WithOtherUserId_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/{id}", testUser2.getId())
                .header("Authorization", "Bearer " + testUser1Token))
                .andExpect(status().isForbidden());

        // Verify user was not deleted
        mockMvc.perform(get("/api/users/{id}", testUser2.getId())
                .header("Authorization", "Bearer " + testUser2Token))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + testUser1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value(testUser1.getUsername()))
                .andExpect(jsonPath("$[0].email").value(testUser1.getEmail()))
                .andExpect(jsonPath("$[1].username").value(testUser2.getUsername()))
                .andExpect(jsonPath("$[1].email").value(testUser2.getEmail()));
    }
} 