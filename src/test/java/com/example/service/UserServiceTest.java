package com.example.service;

import com.example.exception.BadRequestException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.Role;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserValidator userValidator;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, passwordEncoder, userValidator);
    }

    @Test
    public void testCreateUserWithValidUser() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("valid_password");
        user.setBirthDate(LocalDate.of(1990, 1, 1));

        when(userRepository.save(user)).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals(user.getEmail(), createdUser.getEmail());
    }

    @Test
    public void testCreateUserWithInvalidUser() {
        User user = new User();
        user.setFirstName("InvalidFirst");
        user.setLastName("InvalidLast");
        user.setEmail("invalid-email");
        user.setPassword("short");
        user.setBirthDate(LocalDate.now().plusDays(1));

        doThrow(BadRequestException.class).when(userValidator).validateSaveEntity(user);

        assertThrows(BadRequestException.class, () -> userService.createUser(user));

        verify(userRepository, never()).save(user);
    }

    @Test
    public void testFindUserByIdWithExistingUser() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.findById(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
    }

    @Test
    public void testFindUserByIdWithNonExistingUser() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(userId));
    }

    @Test
    public void testUpdateUserWithValidUser() {
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail("existing@example.com");

        User updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setFirstName("UpdatedFirstName");
        updatedUser.setLastName("UpdatedLastName");
        updatedUser.setEmail(existingUser.getEmail());
        updatedUser.setPassword("updated_password");
        updatedUser.setBirthDate(LocalDate.of(1995, 1, 1));
        updatedUser.setRole(Role.ADMIN);

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(updatedUser)).thenReturn(updatedUser);

        User updated = userService.updateUser(updatedUser);

        assertNotNull(updated);
        assertEquals(updatedUser.getFirstName(), updated.getFirstName());
        assertEquals(updatedUser.getLastName(), updated.getLastName());
    }

    @Test
    public void testUpdateUserWithInvalidUser() {
        User existingUser = new User();
        existingUser.setId(UUID.randomUUID());
        existingUser.setEmail("existing@example.com");

        User updatedUser = new User();
        updatedUser.setId(existingUser.getId());
        updatedUser.setFirstName("UpdatedFirstName");
        updatedUser.setLastName("UpdatedLastName");
        updatedUser.setEmail("invalid-email"); // Invalid email
        updatedUser.setPassword("short");
        updatedUser.setBirthDate(LocalDate.now().plusDays(1));

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.of(existingUser));

        doThrow(BadRequestException.class).when(userValidator).validateUpdateEntity(updatedUser);

        assertThrows(BadRequestException.class, () -> userService.updateUser(updatedUser));

        verify(userRepository, never()).save(updatedUser);
    }

    @Test
    public void testUpdateUserWithNonExistentUser() {
        User updatedUser = new User();
        updatedUser.setId(UUID.randomUUID());

        when(userRepository.findById(updatedUser.getId())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(updatedUser));
    }
}
