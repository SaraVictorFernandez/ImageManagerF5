package com.f5.tech_test.mappers;

import com.f5.tech_test.dto.UserDTO;
import com.f5.tech_test.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @InjectMocks
    private UserMapper userMapper;

    private User testUser;
    private UserDTO testUserDTO;
    private static final LocalDateTime NOW = LocalDateTime.now();

    @BeforeEach
    void setUp() {
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
    void toDTO_WithValidUser_ShouldReturnCorrectDTO() {
        // Act
        UserDTO result = userMapper.toDTO(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
    }

    @Test
    void toDTO_WithNullUser_ShouldReturnNull() {
        // Act
        UserDTO result = userMapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void toEntity_WithValidDTO_ShouldReturnCorrectEntity() {
        // Act
        User result = userMapper.toEntity(testUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
    }

    @Test
    void toEntity_WithNullDTO_ShouldReturnNull() {
        // Act
        User result = userMapper.toEntity(null);

        // Assert
        assertNull(result);
    }
} 