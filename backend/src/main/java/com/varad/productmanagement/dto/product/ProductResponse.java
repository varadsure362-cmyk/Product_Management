package com.varad.productmanagement.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Product Response Payload")
public class ProductResponse {

    @Schema(description = "Unique product ID", example = "1")
    private Long id;

    @Schema(description = "Product name", example = "Wireless Gaming Mouse")
    private String productName;

    @Schema(description = "Username of the creator", example = "varad")
    private String createdBy;

    @Schema(description = "Timestamp when the product was created")
    private LocalDateTime createdOn;

    @Schema(description = "Username of the last modifier", example = "varad")
    private String modifiedBy;

    @Schema(description = "Timestamp when the product was last modified")
    private LocalDateTime modifiedOn;
}
