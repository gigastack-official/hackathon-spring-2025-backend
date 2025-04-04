package ru.gigastack.digitalmine.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // Эндпоинт для проверки статуса аутентификации (для страниц "access" и "login")
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(authentication.getName());
        }
        return ResponseEntity.status(401).body("Not authenticated");
    }
}