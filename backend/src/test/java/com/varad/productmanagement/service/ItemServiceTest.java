package com.varad.productmanagement.service;

import com.varad.productmanagement.dto.item.ItemRequest;
import com.varad.productmanagement.dto.item.ItemResponse;
import com.varad.productmanagement.entity.Item;
import com.varad.productmanagement.entity.Product;
import com.varad.productmanagement.exception.ResourceNotFoundException;
import com.varad.productmanagement.mapper.ItemMapper;
import com.varad.productmanagement.repository.ItemRepository;
import com.varad.productmanagement.repository.ProductRepository;
import com.varad.productmanagement.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Product product;
    private Item item;
    private ItemRequest itemRequest;
    private ItemResponse itemResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("Laptop")
                .createdBy("admin")
                .build();

        item = Item.builder()
                .id(10L)
                .product(product)
                .quantity(5)
                .build();

        itemRequest = ItemRequest.builder()
                .quantity(5)
                .build();

        itemResponse = ItemResponse.builder()
                .id(10L)
                .productId(1L)
                .quantity(5)
                .build();
    }

    @Test
    void shouldGetItemsByProductId() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.findByProductId(1L)).thenReturn(List.of(item));
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        List<ItemResponse> result = itemService.getItemsByProductId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getQuantity()).isEqualTo(5);
        verify(itemRepository, times(1)).findByProductId(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundOnGetItems() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemsByProductId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    @Test
    void shouldCreateItemSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemMapper.toEntity(itemRequest, product)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(itemResponse);

        ItemResponse result = itemService.createItem(1L, itemRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getQuantity()).isEqualTo(5);
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void shouldThrowExceptionWhenProductNotFoundOnCreate() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(99L, itemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 99");
    }

    @Test
    void shouldUpdateItemSuccessfully() {
        ItemRequest updateRequest = ItemRequest.builder().quantity(20).build();
        ItemResponse updatedResponse = ItemResponse.builder().id(10L).productId(1L).quantity(20).build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.findByIdAndProductId(10L, 1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toResponse(item)).thenReturn(updatedResponse);

        ItemResponse result = itemService.updateItem(1L, 10L, updateRequest);

        assertThat(result.getQuantity()).isEqualTo(20);
        assertThat(item.getQuantity()).isEqualTo(20);
        verify(itemRepository, times(1)).save(item);
    }

    @Test
    void shouldThrowExceptionWhenItemDoesNotBelongToProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.findByIdAndProductId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(1L, 99L, itemRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Item not found with id: 99 for product id: 1");
    }

    @Test
    void shouldDeleteItemSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.findByIdAndProductId(10L, 1L)).thenReturn(Optional.of(item));
        doNothing().when(itemRepository).delete(item);

        itemService.deleteItem(1L, 10L);

        verify(itemRepository, times(1)).delete(item);
    }

    @Test
    void shouldThrowExceptionWhenDeletingItemFromWrongProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(itemRepository.findByIdAndProductId(99L, 1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.deleteItem(1L, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Item not found with id: 99 for product id: 1");
    }
}
