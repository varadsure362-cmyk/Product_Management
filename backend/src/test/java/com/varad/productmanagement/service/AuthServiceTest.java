package com.varad.productmanagement.service;

import com.varad.productmanagement.dto.auth.AuthResponse;
import com.varad.productmanagement.dto.auth.LoginRequest;
import com.varad.productmanagement.dto.auth.RefreshTokenRequest;
import com.varad.productmanagement.dto.auth.RegisterRequest;
import com.varad.productmanagement.entity.RefreshToken;
import com.varad.productmanagement.entity.Role;
import com.varad.productmanagement.entity.User;
import com.varad.productmanagement.exception.BadRequestException;
import com.varad.productmanagement.repository.RefreshTokenRepository;
import com.varad.productmanagement.repository.UserRepository;
import com.varad.productmanagement.security.CustomUserDetailsService;
import com.varad.productmanagement.security.JwtService;
import com.varad.productmanagement.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;
    private RefreshToken validRefreshToken;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationMs", 604800000L);

        user = User.builder()
                .id(1L)
                .username("john_doe")
                .email("john@example.com")
                .password("encoded_password")
                .role(Role.ROLE_USER)
                .build();

        registerRequest = RegisterRequest.builder()
                .username("john_doe")
                .email("john@example.com")
                .password("password123")
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("john_doe")
                .password("password123")
                .build();

        refreshTokenRequest = RefreshTokenRequest.builder()
                .refreshToken("valid-refresh-token")
                .build();

        validRefreshToken = RefreshToken.builder()
                .id(10L)
                .token("valid-refresh-token")
                .user(user)
                .expiry(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        userDetails = new org.springframework.security.core.userdetails.User(
                "john_doe",
                "encoded_password",
                Collections.emptyList()
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userDetailsService.loadUserByUsername("john_doe")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked_jwt_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validRefreshToken);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked_jwt_token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
        assertThat(response.getUsername()).isEqualTo("john_doe");
        verify(userRepository, times(1)).save(any(User.class));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void shouldThrowExceptionWhenUsernameAlreadyExists() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Username is already taken");
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByUsername("john_doe")).thenReturn(false);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is already registered");
    }

    @Test
    void shouldLoginUserSuccessfully() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByUsername("john_doe")).thenReturn(Optional.of(user));
        when(userDetailsService.loadUserByUsername("john_doe")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("mocked_jwt_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(validRefreshToken);

        AuthResponse response = authService.login(loginRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("mocked_jwt_token");
        assertThat(response.getRefreshToken()).isEqualTo("valid-refresh-token");
    }

    @Test
    void shouldThrowExceptionWhenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void shouldRefreshAccessTokenSuccessfullyWithRotation() {
        RefreshToken newRefreshTokenObj = RefreshToken.builder()
                .id(11L)
                .token("new-refresh-token")
                .user(user)
                .expiry(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validRefreshToken));
        when(userDetailsService.loadUserByUsername("john_doe")).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("new_access_token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(newRefreshTokenObj);

        AuthResponse response = authService.refreshToken(refreshTokenRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new_access_token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(validRefreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any(RefreshToken.class));
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        RefreshToken expiredToken = RefreshToken.builder()
                .id(10L)
                .token("expired-token")
                .user(user)
                .expiry(Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("expired-token").build();

        assertThatThrownBy(() -> authService.refreshToken(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Refresh token was expired");
    }

    @Test
    void shouldRejectRevokedRefreshToken() {
        RefreshToken revokedToken = RefreshToken.builder()
                .id(10L)
                .token("revoked-token")
                .user(user)
                .expiry(Instant.now().plusSeconds(3600))
                .revoked(true)
                .build();

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(revokedToken));

        RefreshTokenRequest req = RefreshTokenRequest.builder().refreshToken("revoked-token").build();

        assertThatThrownBy(() -> authService.refreshToken(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Refresh token was revoked");
    }

    @Test
    void shouldRevokeRefreshTokenOnLogout() {
        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(validRefreshToken));

        authService.logout(refreshTokenRequest);

        assertThat(validRefreshToken.isRevoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(validRefreshToken);
    }
}
