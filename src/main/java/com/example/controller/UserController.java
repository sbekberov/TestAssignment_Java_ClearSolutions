package com.example.controller;

import com.example.exception.ResourceNotFoundException;
import com.example.model.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable UUID id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found"));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable UUID id, @RequestBody User resource) {
        User entity = userService.findById(id);
        if (entity == null) throw new ResourceNotFoundException();
        resource.setId(id);
        User result = userService.updateUser(resource);
        return new ResponseEntity(result, HttpStatus.OK);

    }


    @DeleteMapping("/{id}")
    public HttpStatus deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return HttpStatus.OK;
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsersByBirthDateRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to) {
        if (from.isBefore(to)) {
            List<User> users = userService.findUsersByBirthDateRange(from, to);
            return ResponseEntity.ok(users);
        } else {
            throw new IllegalArgumentException("'From' should be less than 'To'");
        }
    }
}
