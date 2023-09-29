package com.example.validator;

import com.example.exception.BadRequestException;
import com.example.exception.ResourceNotFoundException;
import com.example.model.User;
import com.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 50;

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 30;


    @Value("${user.age.limit}")
    private int userAgeLimit;
    private final UserRepository repository;

    public UserValidator(UserRepository repository) {
        this.repository = repository;
    }

    private void validateFirstName(StringBuilder exceptions, String firstName) {
        if (firstName.length() < MIN_NAME_LENGTH || firstName.length() > MAX_NAME_LENGTH) {
            exceptions.append("The firstname field must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters long.\n");
        }
    }

    private void validateLastName(StringBuilder exceptions, String lastName) {
        if (lastName.length() < MIN_NAME_LENGTH || lastName.length() > MAX_NAME_LENGTH) {
            exceptions.append("The lastname field must be between " + MIN_NAME_LENGTH + " and " + MAX_NAME_LENGTH + " characters long.\n");
        }
    }

    private void validateEmail(StringBuilder exceptions, String email) {
        if (repository.existsByEmail(email)) {
            exceptions.append("Email is already in use!\n");
        } else if (!isValidEmail(email)) {
            exceptions.append("The email field should look like email. For example: bekberov@gmail.com\n");
        }
    }

    private void validatePassword(StringBuilder exceptions, String password) {
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            exceptions.append("The password field must be between " + MIN_PASSWORD_LENGTH + " and " + MAX_PASSWORD_LENGTH + " characters long.\n");
        }
    }



    private void validateBirthDate(StringBuilder exceptions, LocalDate birthDate) {
        LocalDate currentDate = LocalDate.now();
        if (currentDate.isBefore(birthDate)) {
            exceptions.append("Birthdate cannot be in the future.\n");
        } else {
            int age = Period.between(birthDate, currentDate).getYears();
            if (age < userAgeLimit) {
                exceptions.append("User must be older than " + userAgeLimit + " years.\n");
            }
        }
    }

    private boolean isValidEmail(String email) {
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        return matcher.find();
    }

    public void throwException(StringBuilder exceptions) {
        if (exceptions.length() != 0) {
            throw new BadRequestException(exceptions.toString());
        }
    }

    public void validateSaveEntity(User entity) {
        StringBuilder exceptions = new StringBuilder();
        validateFirstName(exceptions, entity.getFirstName());
        validateLastName(exceptions, entity.getLastName());
        validateEmail(exceptions, entity.getEmail());
        validatePassword(exceptions, entity.getPassword());
        validateBirthDate(exceptions, entity.getBirthDate());
        throwException(exceptions);
    }

    public void validateUpdateEntity(User entity) {
        StringBuilder exceptions = new StringBuilder();
        var oldUser = repository.findById(entity.getId());
        if (oldUser.isEmpty()) {
            throw new ResourceNotFoundException("Cannot update non-existent user!");
        }
        if (!oldUser.get().getEmail().equals(entity.getEmail())) {
            exceptions.append("The email field cannot be updated!");
        }
        validateFirstName(exceptions, entity.getFirstName());
        validateLastName(exceptions, entity.getLastName());
        validatePassword(exceptions, entity.getPassword());
        validateBirthDate(exceptions, entity.getBirthDate());
        throwException(exceptions);
    }
}
