package com.varad.productmanagement.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standardized Error Response Body")
public class ErrorResponse {

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "HTTP status error phrase", example = "Bad Request")
    private String error;

    @Schema(description = "Error message description", example = "Validation failed")
    private String message;

    @Schema(description = "Request URI path", example = "/api/v1/products")
    private String path;

    @Schema(description = "Map of field-level validation errors (if applicable)")
    private Map<String, String> errors;
}
