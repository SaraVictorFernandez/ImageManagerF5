package com.f5.tech_test.controllers;

import com.f5.tech_test.dto.LoginRequest;
import com.f5.tech_test.dto.LoginResponse;
import com.f5.tech_test.dto.RegisterRequest;
import com.f5.tech_test.dto.UserDTO;
import com.f5.tech_test.exceptions.UserAlreadyExistsException;
import com.f5.tech_test.services.JwtService;
import com.f5.tech_test.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager, 
                         JwtService jwtService,
                         UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO registeredUser = userService.registerUser(request);
        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/change-password")
    public ResponseEntity<LoginResponse> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> request) {
        String username = authentication.getName();
        String newPassword = request.get("newPassword");
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Update the password
        UserDTO user = userService.getUserByUsername(username);
        userService.updatePassword(user.getId(), newPassword);

        // Generate new token with updated credentials
        Authentication newAuthentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, newPassword)
        );

        UserDetails userDetails = (UserDetails) newAuthentication.getPrincipal();
        String newToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(newToken));
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExistsException(UserAlreadyExistsException e) {
        return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
    }
} 