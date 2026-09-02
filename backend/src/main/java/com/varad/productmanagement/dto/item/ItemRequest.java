package com.varad.productmanagement.dto.item;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Item Request Payload")
public class ItemRequest {

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must not be negative")
    @Schema(description = "Item quantity (non-negative integer)", example = "150")
    private Integer quantity;
}
