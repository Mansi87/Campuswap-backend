package com.campusswap.backend.service;

import com.campusswap.backend.dto.AuthResponse;
import com.campusswap.backend.dto.LoginRequest;
import com.campusswap.backend.dto.RegisterRequest;
import com.campusswap.backend.model.College;
import com.campusswap.backend.model.User;
import com.campusswap.backend.repository.CollegeRepository;
import com.campusswap.backend.repository.UserRepository;
import com.campusswap.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Get college
        College college = collegeRepository.findById(UUID.fromString(request.getCollegeId()))
                .orElseThrow(() -> new RuntimeException("College not found"));

        // Verify email domain matches college
        String emailDomain = request.getEmail().substring(request.getEmail().indexOf("@") + 1);
        if (!emailDomain.equals(college.getEmailDomain())) {
            throw new RuntimeException("Email domain must match college domain: " + college.getEmailDomain());
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setCollege(college);
        user.setIsVerified(true);  // Auto-verify for now (add email verification later)

        user = userRepository.save(user);

        // Generate token
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getId().toString(),
                "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest request) {
        // Find user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid email or password");
        }

        // Generate token
        String token = jwtUtil.generateToken(user.getEmail(), user.getId());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getId().toString(),
                "Login successful"
        );
    }
}
