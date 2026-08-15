package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.LoginRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.RegisterRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.AuthResponse;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.entity.enums.UserRole;
import com.adegadopaibackend.adegadopaibackend.exception.ConflictException;
import com.adegadopaibackend.adegadopaibackend.mapper.UserMapper;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
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
        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }

        var user = User.builder()
                .name(request.name())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();

        var savedUser = userRepository.save(user);

        var accessToken = jwtService.generateToken(savedUser, savedUser.getId(), List.of(savedUser.getRole().name()));
        var refreshToken = jwtService.generateRefreshToken(savedUser);

        return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(savedUser));
    }

    public AuthResponse authenticate(LoginRequest request) {
        String email = normalizeEmail(request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password())
        );

        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciais inválidas"));

        var accessToken = jwtService.generateToken(user, user.getId(), List.of(user.getRole().name()));
        var refreshToken = jwtService.generateRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
    }

    public AuthResponse refreshToken(String refreshToken) {
        final String userEmail = normalizeEmail(jwtService.extractUsername(refreshToken));

        if (userEmail != null) {
            var user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new BadCredentialsException("Refresh token inválido"));

            if (jwtService.isTokenValid(refreshToken, user)) {
                var accessToken = jwtService.generateToken(user, user.getId(), List.of(user.getRole().name()));
                return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
            }
        }
        throw new BadCredentialsException("Refresh token inválido");
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim();
    }
}