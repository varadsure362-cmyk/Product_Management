package com.varad.productmanagement.service;

import com.varad.productmanagement.dto.item.ItemRequest;
import com.varad.productmanagement.dto.item.ItemResponse;

import java.util.List;

public interface ItemService {

    List<ItemResponse> getItemsByProductId(Long productId);

    ItemResponse createItem(Long productId, ItemRequest request);

    ItemResponse updateItem(Long productId, Long itemId, ItemRequest request);

    void deleteItem(Long productId, Long itemId);
}
