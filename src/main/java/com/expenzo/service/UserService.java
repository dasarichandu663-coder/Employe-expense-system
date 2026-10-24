package com.expenzo.service;

import com.expenzo.dto.AuthRequest;
import com.expenzo.dto.AuthResponse;
import com.expenzo.dto.RegisterRequest;
import com.expenzo.entity.User;
import com.expenzo.enums.Role;
import com.expenzo.exception.BadRequestException;
import com.expenzo.exception.ResourceNotFoundException;
import com.expenzo.exception.UnauthorizedException;
import com.expenzo.repository.UserRepository;
import com.expenzo.security.JwtUtil;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        String email = req.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("An account with this corporate email already exists.");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setDepartment(req.getDepartment() != null ? req.getDepartment().trim() : "General");
        user.setPhone(req.getPhone());
        user.setRole(req.getRole() != null ? req.getRole() : Role.ROLE_EMPLOYEE);
        user.setActive(true);

        User saved = userRepository.save(user);

        // Generate JWT token for immediate seamless login
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        saved.getEmail(),
                        saved.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority(saved.getRole().name()))
                );

        String token = jwtUtil.generateToken(userDetails, saved.getRole().name(), saved.getId());

        return new AuthResponse(
                token,
                saved.getId(),
                saved.getFullName(),
                saved.getEmail(),
                saved.getRole().name(),
                saved.getDepartment()
        );
    }

    public AuthResponse login(AuthRequest req) {
        String email = req.getEmail().toLowerCase().trim();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (!user.isActive()) {
            throw new UnauthorizedException("Your account has been deactivated. Please contact your manager.");
        }

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPassword(),
                        Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()))
                );

        String token = jwtUtil.generateToken(userDetails, user.getRole().name(), user.getId());

        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getDepartment()
        );
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    public List<User> getAllEmployees() {
        return userRepository.findAllByOrderByCreatedAtDesc();
    }
}
