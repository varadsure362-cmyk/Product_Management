package com.varad.productmanagement.dto.item;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Item Response Payload")
public class ItemResponse {

    @Schema(description = "Unique item ID", example = "10")
    private Long id;

    @Schema(description = "Associated product ID", example = "1")
    private Long productId;

    @Schema(description = "Item quantity", example = "150")
    private Integer quantity;
}
