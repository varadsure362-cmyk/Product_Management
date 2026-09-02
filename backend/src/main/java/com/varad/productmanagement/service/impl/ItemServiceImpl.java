package com.varad.productmanagement.service.impl;

import com.varad.productmanagement.dto.item.ItemRequest;
import com.varad.productmanagement.dto.item.ItemResponse;
import com.varad.productmanagement.entity.Item;
import com.varad.productmanagement.entity.Product;
import com.varad.productmanagement.exception.ResourceNotFoundException;
import com.varad.productmanagement.mapper.ItemMapper;
import com.varad.productmanagement.repository.ItemRepository;
import com.varad.productmanagement.repository.ProductRepository;
import com.varad.productmanagement.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ProductRepository productRepository;
    private final ItemMapper itemMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ItemResponse> getItemsByProductId(Long productId) {
        findProductById(productId);
        return itemRepository.findByProductId(productId).stream()
                .map(itemMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ItemResponse createItem(Long productId, ItemRequest request) {
        Product product = findProductById(productId);
        Item item = itemMapper.toEntity(request, product);
        Item saved = itemRepository.save(item);
        return itemMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ItemResponse updateItem(Long productId, Long itemId, ItemRequest request) {
        findProductById(productId);
        Item item = findItemByIdAndProductId(itemId, productId);
        item.setQuantity(request.getQuantity());
        Item updated = itemRepository.save(item);
        return itemMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteItem(Long productId, Long itemId) {
        findProductById(productId);
        Item item = findItemByIdAndProductId(itemId, productId);
        itemRepository.delete(item);
    }

    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));
    }

    private Item findItemByIdAndProductId(Long itemId, Long productId) {
        return itemRepository.findByIdAndProductId(itemId, productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item not found with id: " + itemId + " for product id: " + productId));
    }
}
