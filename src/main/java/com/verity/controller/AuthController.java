package com.verity.controller;

import com.verity.dto.LoginRequest;
import com.verity.dto.RegisterRequest;
import com.verity.model.User;
import com.verity.service.UserService;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final String COOKIE_NAME =
            "VERITY_SESSION";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

        try {

            User user = new User(
                    request.getName(),
                    request.getEmail(),
                    request.getPassword(),
                    request.getRole()
            );

            User savedUser =
                    userService.registerUser(user);

            return ResponseEntity.ok(savedUser);

        } catch (Exception e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        try {

            User user =
                    userService.loginUser(
                            request.getEmail(),
                            request.getPassword()
                    );

            ResponseCookie cookie =
                    ResponseCookie
                            .from(
                                    COOKIE_NAME,
                                    user.getSessionToken()
                            )
                            .httpOnly(true)
                            .secure(false)
                            .path("/")
                            .maxAge(60 * 60 * 24)
                            .sameSite("Lax")
                            .build();

            return ResponseEntity
                    .ok()
                    .header(
                            "Set-Cookie",
                            cookie.toString()
                    )
                    .body(user);

        } catch (Exception e) {

            return ResponseEntity
                    .status(401)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(
                    value = COOKIE_NAME,
                    required = false
            )
            String token) {

        if (token != null &&
                !token.isBlank()) {

            userService.logout(token);
        }

        ResponseCookie expiredCookie =
                ResponseCookie
                        .from(
                                COOKIE_NAME,
                                ""
                        )
                        .httpOnly(true)
                        .secure(false)
                        .path("/")
                        .maxAge(0)
                        .sameSite("Lax")
                        .build();

        return ResponseEntity
                .ok()
                .header(
                        "Set-Cookie",
                        expiredCookie.toString()
                )
                .body("Logged out successfully");
    }
}