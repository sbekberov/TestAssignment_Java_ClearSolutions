package com.example.controller;

import com.example.dto.AuthenticationRequestDTO;
import com.example.exception.BadRequestException;
import com.example.model.User;
import com.example.security.JwtTokenProvider;
import com.example.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthenticationController {

    private final UserService service;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationController(UserService service, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/api/register")
    public ResponseEntity<String> registration(@Valid @RequestBody User resource) {
        try {
            service.register(resource);
            log.debug("Registration a new user.");
            return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
        } catch (BadRequestException | IllegalArgumentException e) {
            StringBuilder exceptions = new StringBuilder();
            exceptions.append(e.getMessage());
            return new ResponseEntity<>(exceptions.toString(), HttpStatus.BAD_REQUEST);
        }
    }




    @PostMapping("/api/login")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequestDTO request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            User user = service.findByEmail(request.getEmail());
            String token = jwtTokenProvider.createToken(request.getEmail(), user.getPassword());
            Map<Object, Object> response = new HashMap<>();
            response.put("email", request.getEmail());
            response.put("token", token);
            log.debug("Login user.", user);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            log.error("Invalid email/password combination");
            return new ResponseEntity<>("Invalid email/password combination", HttpStatus.FORBIDDEN);
        }
    }

    @PostMapping("/api/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();
        securityContextLogoutHandler.logout(request, response, null);
    }
}
