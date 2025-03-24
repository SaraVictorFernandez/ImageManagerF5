package com.f5.tech_test.services;

import com.f5.tech_test.dto.UserDTO;
import com.f5.tech_test.dto.RegisterRequest;
import com.f5.tech_test.entities.User;
import com.f5.tech_test.exceptions.UserAlreadyExistsException;
import com.f5.tech_test.exceptions.UserNotFoundException;
import com.f5.tech_test.mappers.UserMapper;
import com.f5.tech_test.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;
    private RegisterRequest testRegisterRequest;
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final String ENCODED_PASSWORD = "encodedPassword123";

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

        testRegisterRequest = new RegisterRequest();
        testRegisterRequest.setUsername("testuser");
        testRegisterRequest.setEmail("test@example.com");
        testRegisterRequest.setPassword("password123");

        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
    }

    @Test
    void registerUser_WithValidUser_ShouldReturnUserDTO() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntity(any(RegisterRequest.class))).thenReturn(testUser);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.registerUser(testRegisterRequest);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository).existsByUsername(testRegisterRequest.getUsername());
        verify(userRepository).existsByEmail(testRegisterRequest.getEmail());
        verify(passwordEncoder).encode(testRegisterRequest.getPassword());
        verify(userMapper).toEntity(testRegisterRequest);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDTO(testUser);
    }

    @Test
    void registerUser_WithExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(testRegisterRequest));
        verify(userRepository).existsByUsername(testRegisterRequest.getUsername());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(testRegisterRequest));
        verify(userRepository).existsByUsername(testRegisterRequest.getUsername());
        verify(userRepository).existsByEmail(testRegisterRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_WithExistingUser_ShouldReturnUserDTO() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository).findById(1L);
        verify(userMapper).toDTO(testUser);
    }

    @Test
    void getUserById_WithNonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
        verify(userRepository).findById(1L);
        verify(userMapper, never()).toDTO(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDTOs() {
        // Arrange
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // Act
        List<UserDTO> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDTO.getId(), result.get(0).getId());
        assertEquals(testUserDTO.getUsername(), result.get(0).getUsername());
        assertEquals(testUserDTO.getEmail(), result.get(0).getEmail());
        verify(userRepository).findAll();
        verify(userMapper).toDTO(testUser);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_WithExistingUser_ShouldReturnUpdatedUserDTO() {
        // Arrange
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setUsername("newusername");
        updatedUserDTO.setEmail("newemail@example.com");

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

        // Act
        UserDTO result = userService.updateUser(1L, updatedUserDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testUserDTO.getId(), result.getId());
        assertEquals(testUserDTO.getUsername(), result.getUsername());
        assertEquals(testUserDTO.getEmail(), result.getEmail());
        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verify(userMapper).toDTO(any(User.class));
    }

    @Test
    void updateUser_WithNonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, testUserDTO));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).save(any(User.class));
        verify(userMapper, never()).toDTO(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteUser_WithExistingUser_ShouldDeleteSuccessfully() {
        // Arrange
        when(userRepository.existsById(anyLong())).thenReturn(true);

        // Act & Assert
        assertDoesNotThrow(() -> userService.deleteUser(1L));
        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void deleteUser_WithNonExistingUser_ShouldThrowException() {
        // Arrange
        when(userRepository.existsById(anyLong())).thenReturn(false);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(anyLong());
        verify(passwordEncoder, never()).encode(anyString());
    }
} 