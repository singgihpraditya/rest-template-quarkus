package com.example.template.service;

import com.example.template.dto.request.LoginRequest;
import com.example.template.dto.request.RegisterRequest;
import com.example.template.dto.response.TokenResponse;
import com.example.template.dto.response.UserResponse;
import com.example.template.entity.Role;
import com.example.template.entity.User;
import com.example.template.exception.BusinessException;
import com.example.template.exception.ResourceNotFoundException;
import com.example.template.exception.UnauthorizedException;
import com.example.template.repository.RoleRepository;
import com.example.template.repository.UserRepository;
import com.example.template.security.JwtTokenProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Set;

/**
 * AuthService untuk Quarkus.
 *
 * Perbedaan dari Spring Boot:
 * - Tidak menggunakan AuthenticationManager → manual password verify dengan jBCrypt
 * - generateToken() menerima User entity (bukan UserDetails)
 * - @Inject menggantikan @RequiredArgsConstructor
 */
@ApplicationScoped
@Slf4j
public class AuthService {

    @Inject
    JwtTokenProvider jwtTokenProvider;

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;

    @ConfigProperty(name = "app.jwt.expiration")
    long jwtExpiration;

    /**
     * Login: verifikasi password secara manual, generate JWT token.
     */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Username atau password salah"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Akun tidak aktif");
        }

        if (!BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Username atau password salah");
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("User berhasil login: {}", user.getUsername());

        return TokenResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .user(UserResponse.from(user))
                .build();
    }

    /**
     * Register: daftarkan user baru dengan role ROLE_USER.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Username '" + request.getUsername() + "' sudah digunakan");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email '" + request.getEmail() + "' sudah terdaftar");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_USER tidak ditemukan"));

        User newUser = User.builder()
                .username(request.getUsername())
                .password(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .active(true)
                .roles(Set.of(userRole))
                .build();

        userRepository.persist(newUser);
        log.info("User baru terdaftar: {}", newUser.getUsername());
        return UserResponse.from(newUser);
    }

    /**
     * Ambil data user yang sedang login berdasarkan username dari JWT.
     */
    @Transactional
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User tidak ditemukan: " + username));
        return UserResponse.from(user);
    }
}
