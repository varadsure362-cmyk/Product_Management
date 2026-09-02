package com.varad.productmanagement.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication Response Payload")
public class AuthResponse {

    @Schema(description = "JWT Access Token used for Bearer authentication", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Refresh token used to obtain new access tokens", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String refreshToken;

    @Builder.Default
    @Schema(description = "Token type prefix", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "Authenticated username", example = "johndoe")
    private String username;

    @Schema(description = "Assigned user role", example = "ROLE_USER")
    private String role;
}
