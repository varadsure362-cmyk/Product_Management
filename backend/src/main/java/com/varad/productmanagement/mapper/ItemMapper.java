package com.varad.productmanagement.mapper;

import com.varad.productmanagement.dto.item.ItemRequest;
import com.varad.productmanagement.dto.item.ItemResponse;
import com.varad.productmanagement.entity.Item;
import com.varad.productmanagement.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public Item toEntity(ItemRequest request, Product product) {
        return Item.builder()
                .product(product)
                .quantity(request.getQuantity())
                .build();
    }

    public ItemResponse toResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .quantity(item.getQuantity())
                .build();
    }
}
