package com.varad.productmanagement.controller;

import com.varad.productmanagement.dto.item.ItemRequest;
import com.varad.productmanagement.dto.item.ItemResponse;
import com.varad.productmanagement.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products/{productId}/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Endpoints for managing items associated with products")
@SecurityRequirement(name = "bearerAuth")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "Get all items for a product", description = "Returns all items associated with a given product ID. Accessible by USER and ADMIN roles.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved items",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ItemResponse.class))))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<List<ItemResponse>> getItems(@PathVariable Long productId) {
        return ResponseEntity.ok(itemService.getItemsByProductId(productId));
    }

    @PostMapping
    @Operation(summary = "Create an item for a product", description = "Creates a new item for a given product ID. Accessible by ADMIN role only.")
    @ApiResponse(responseCode = "201", description = "Item created successfully",
            content = @Content(schema = @Schema(implementation = ItemResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request payload (e.g. negative quantity)")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<ItemResponse> createItem(
            @PathVariable Long productId,
            @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(productId, request));
    }

    @PutMapping("/{itemId}")
    @Operation(summary = "Update an item", description = "Updates an item belonging to a given product ID. Accessible by ADMIN role only.")
    @ApiResponse(responseCode = "200", description = "Item updated successfully",
            content = @Content(schema = @Schema(implementation = ItemResponse.class)))
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "Product or Item not found")
    public ResponseEntity<ItemResponse> updateItem(
            @PathVariable Long productId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(productId, itemId, request));
    }

    @DeleteMapping("/{itemId}")
    @Operation(summary = "Delete an item", description = "Deletes an item belonging to a given product ID. Accessible by ADMIN role only.")
    @ApiResponse(responseCode = "204", description = "Item deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - Requires ADMIN role")
    @ApiResponse(responseCode = "404", description = "Product or Item not found")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Long productId,
            @PathVariable Long itemId) {
        itemService.deleteItem(productId, itemId);
        return ResponseEntity.noContent().build();
    }
}
