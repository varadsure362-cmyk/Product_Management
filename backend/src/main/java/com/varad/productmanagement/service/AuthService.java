package com.varad.productmanagement.service;

import com.varad.productmanagement.dto.auth.AuthResponse;
import com.varad.productmanagement.dto.auth.LoginRequest;
import com.varad.productmanagement.dto.auth.RefreshTokenRequest;
import com.varad.productmanagement.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
