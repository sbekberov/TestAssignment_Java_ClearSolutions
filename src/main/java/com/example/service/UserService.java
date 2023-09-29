package com.example.service;

import com.example.exception.BadRequestException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.User;
import com.example.repository.UserRepository;
import com.example.validator.UserValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, UserValidator userValidator) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.passwordEncoder = passwordEncoder;
    }

    private void encodePassword(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User register(User user) {
        encodePassword(user);
        return createUser(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        userValidator.validateSaveEntity(user);
        try {
            return userRepository.save(user);
        } catch (RuntimeException e) {
            throw new BadRequestException(e.getMessage());
        }
    }



    public User updateUser(User updatedUser) {
        User oldUser = findById(updatedUser.getId());
        if (updatedUser.getFirstName() == null) {
            updatedUser.setFirstName(oldUser.getFirstName());
        }
        if (updatedUser.getLastName() == null) {
            updatedUser.setLastName(oldUser.getLastName());
        }
        if (updatedUser.getEmail() == null) {
            updatedUser.setEmail(oldUser.getEmail());
        }
        if (updatedUser.getEmail() != oldUser.getEmail() ) {
            throw new BadRequestException("Email can't be changed");
        }
        if (updatedUser.getBirthDate() == null) {
            updatedUser.setBirthDate(oldUser.getBirthDate());
        }
        if (updatedUser.getAddress() == null) {
            updatedUser.setAddress(oldUser.getAddress());
        }
        if (updatedUser.getPhoneNumber() == null) {
            updatedUser.setPhoneNumber(oldUser.getPhoneNumber());
        }
        if (updatedUser.getRole() == null) {
            updatedUser.setRole(oldUser.getRole());
        }
        if (updatedUser.getPassword() == null) {
            updatedUser.setPassword(oldUser.getPassword());
        }

        userValidator.validateUpdateEntity(updatedUser);

        try {
            return userRepository.save(updatedUser);
        } catch (RuntimeException e){
            throw new BadRequestException(e.getMessage());
        }

    }

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    public List<User> findUsersByBirthDateRange(LocalDate from, LocalDate to) {
        return userRepository.findByBirthDateBetween(from, to);
    }
}
