package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.LoginRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.RegisterRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.AuthResponse;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.entity.enums.UserRole;
import com.adegadopaibackend.adegadopaibackend.mapper.UserMapper;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;

    public AuthResponse register(RegisterRequest request) {
        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();

        userRepository.save(user);

        // Passando ID e Role para o token
        var accessToken = jwtService.generateToken(user, user.getId(), List.of(user.getRole().name()));
        var refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Passando ID e Role para o token
        var accessToken = jwtService.generateToken(user, user.getId(), List.of(user.getRole().name()));
        var refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
    }

    public AuthResponse refreshToken(String refreshToken) {
        final String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                // Passando ID e Role novamente na renovação
                var accessToken = jwtService.generateToken(user, user.getId(), List.of(user.getRole().name()));
                return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
            }
        }
        throw new RuntimeException("Refresh Token invalid");
    }
}