package com.verity.service;

import com.verity.model.User;
import com.verity.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User registerUser(User user) {

        if (user.getEmail() == null ||
                user.getEmail().isBlank()) {

            throw new RuntimeException("Email is required");
        }

        if (user.getPassword() == null ||
                user.getPassword().isBlank()) {

            throw new RuntimeException("Password is required");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        if (user.getRole() == null ||
                user.getRole().isBlank()) {

            user.setRole("EMPLOYEE");
        }

        user.setSessionToken(null);

        return userRepository.save(user);
    }

    public User loginUser(
            String email,
            String password) {

        if (email == null ||
                email.isBlank() ||
                password == null ||
                password.isBlank()) {

            throw new RuntimeException(
                    "Email and password are required"
            );
        }

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Invalid email or password"
                        )
                );

        if (!user.getPassword().equals(password)) {

            throw new RuntimeException(
                    "Invalid email or password"
            );
        }

        if (user.getRole() == null ||
                user.getRole().isBlank()) {

            user.setRole("EMPLOYEE");
        }

        String token =
                UUID.randomUUID().toString();

        user.setSessionToken(token);

        return userRepository.save(user);
    }

    public User findByToken(String token) {

        if (token == null ||
                token.isBlank()) {

            return null;
        }

        return userRepository
                .findBySessionToken(token)
                .orElse(null);
    }

    public void logout(String token) {

        User user = findByToken(token);

        if (user != null) {

            user.setSessionToken(null);

            userRepository.save(user);
        }
    }
}