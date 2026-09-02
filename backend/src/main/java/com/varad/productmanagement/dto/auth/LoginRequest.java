package com.varad.productmanagement.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "User Login Request Payload")
public class LoginRequest {

    @JsonAlias({"username", "email"})
    @NotBlank(message = "Username or email is required")
    @Schema(description = "Username or Email address", example = "johndoe")
    private String usernameOrEmail;

    @NotBlank(message = "Password is required")
    @Schema(description = "User password", example = "password123")
    private String password;
}
