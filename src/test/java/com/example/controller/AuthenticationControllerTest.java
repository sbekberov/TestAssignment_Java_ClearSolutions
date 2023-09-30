package com.example.controller;

import com.example.dto.AuthenticationRequestDTO;
import com.example.exception.BadRequestException;
import com.example.model.User;
import com.example.security.JwtTokenProvider;
import com.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthenticationControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthenticationController authenticationController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationController = new AuthenticationController(userService, authenticationManager, jwtTokenProvider);
    }

    @Test
    public void testRegistrationWithValidUser() {
        User user = new User();
        user.setEmail("valid@example.com");
        user.setPassword("valid_password");

        when(userService.register(user)).thenReturn(user);

        ResponseEntity<String> response = authenticationController.registration(user);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
    }

    @Test
    public void testRegistrationWithInvalidUser() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setPassword("short");

        doThrow(BadRequestException.class).when(userService).register(user);

        ResponseEntity<String> response = authenticationController.registration(user);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testAuthenticateWithValidCredentials() {
        AuthenticationRequestDTO request = new AuthenticationRequestDTO();
        request.setEmail("valid@example.com");
        request.setPassword("valid_password");

        User user = new User();
        user.setEmail(request.getEmail());

        Authentication authentication = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        when(authenticationManager.authenticate(authentication)).thenReturn(authentication);

        when(userService.findByEmail(request.getEmail())).thenReturn(user);
        when(jwtTokenProvider.createToken(request.getEmail(), user.getPassword())).thenReturn("valid_token");

        ResponseEntity<?> response = authenticationController.authenticate(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }


}
