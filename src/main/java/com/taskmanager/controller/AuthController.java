package com.taskmanager.controller;

import com.taskmanager.dto.AuthDto;
import com.taskmanager.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody AuthDto.SignupRequest request) {
        try {
            AuthDto.AuthResponse response = authService.signup(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(AuthDto.MessageResponse.builder()
                            .message(e.getMessage())
                            .success(false)
                            .build());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthDto.LoginRequest request) {
        try {
            AuthDto.AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(AuthDto.MessageResponse.builder()
                            .message("Invalid credentials")
                            .success(false)
                            .build());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        try {
            var user = authService.getCurrentUser();
            return ResponseEntity.ok(AuthDto.AuthResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getGlobalRole().name())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(AuthDto.MessageResponse.builder()
                    .message("Unauthorized").success(false).build());
        }
    }
}
