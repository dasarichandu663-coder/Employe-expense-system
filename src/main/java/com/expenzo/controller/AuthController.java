package com.expenzo.controller;

import com.expenzo.dto.AuthRequest;
import com.expenzo.dto.AuthResponse;
import com.expenzo.dto.RegisterRequest;
import com.expenzo.entity.User;
import com.expenzo.security.SecurityUtils;
import com.expenzo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userService.getUserByEmail(email);
        // Scrub password hash before sending to client
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/employees")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<User>> getAllEmployees() {
        List<User> list = userService.getAllEmployees();
        list.forEach(u -> u.setPassword(null));
        return ResponseEntity.ok(list);
    }
}
