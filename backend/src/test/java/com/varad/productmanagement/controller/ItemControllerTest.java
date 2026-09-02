package com.varad.productmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.varad.productmanagement.config.SecurityConfig;
import com.varad.productmanagement.dto.item.ItemRequest;
import com.varad.productmanagement.dto.item.ItemResponse;
import com.varad.productmanagement.exception.GlobalExceptionHandler;
import com.varad.productmanagement.exception.ResourceNotFoundException;
import com.varad.productmanagement.security.CustomUserDetailsService;
import com.varad.productmanagement.security.JwtService;
import com.varad.productmanagement.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@ActiveProfiles("test")
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    private ItemRequest validItemRequest;
    private ItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        validItemRequest = ItemRequest.builder()
                .quantity(10)
                .build();

        itemResponse = ItemResponse.builder()
                .id(1L)
                .productId(100L)
                .quantity(10)
                .build();
    }

    @Test
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/products/100/items"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldAllowUserToGetItems() throws Exception {
        when(itemService.getItemsByProductId(100L)).thenReturn(List.of(itemResponse));

        mockMvc.perform(get("/api/v1/products/100/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].quantity").value(10));
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToCreateItem() throws Exception {
        mockMvc.perform(post("/api/v1/products/100/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToCreateItem() throws Exception {
        when(itemService.createItem(eq(100L), any(ItemRequest.class))).thenReturn(itemResponse);

        mockMvc.perform(post("/api/v1/products/100/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldRejectInvalidNegativeQuantity() throws Exception {
        ItemRequest invalidRequest = ItemRequest.builder().quantity(-5).build();

        mockMvc.perform(post("/api/v1/products/100/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.quantity").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToUpdateItem() throws Exception {
        when(itemService.updateItem(eq(100L), eq(1L), any(ItemRequest.class))).thenReturn(itemResponse);

        mockMvc.perform(put("/api/v1/products/100/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToDeleteItem() throws Exception {
        mockMvc.perform(delete("/api/v1/products/100/items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void shouldReturn404WhenProductNotFound() throws Exception {
        when(itemService.getItemsByProductId(999L))
                .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

        mockMvc.perform(get("/api/v1/products/999/items"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
    }
}
