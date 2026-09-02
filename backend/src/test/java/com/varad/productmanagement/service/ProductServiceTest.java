package com.varad.productmanagement.service;

import com.varad.productmanagement.dto.common.PagedResponse;
import com.varad.productmanagement.dto.product.ProductRequest;
import com.varad.productmanagement.dto.product.ProductResponse;
import com.varad.productmanagement.entity.Product;
import com.varad.productmanagement.exception.ResourceNotFoundException;
import com.varad.productmanagement.mapper.ProductMapper;
import com.varad.productmanagement.repository.ProductRepository;
import com.varad.productmanagement.service.impl.ProductServiceImpl;
import com.varad.productmanagement.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private SecurityUtil securityUtil;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductRequest productRequest;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .productName("Test Product")
                .createdBy("system")
                .createdOn(LocalDateTime.now())
                .build();

        productRequest = ProductRequest.builder()
                .productName("Test Product")
                .build();

        productResponse = ProductResponse.builder()
                .id(1L)
                .productName("Test Product")
                .createdBy("system")
                .createdOn(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateProductSuccessfully() {
        when(securityUtil.getCurrentUsername()).thenReturn("system");
        when(productMapper.toEntity(any(ProductRequest.class), anyString())).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse response = productService.createProduct(productRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getProductName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldGetProductByIdSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        ProductResponse response = productService.getProductById(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 1");
    }

    @Test
    void shouldGetFirstPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        PagedResponse<ProductResponse> pagedResponse = productService.getProducts(0, 10);

        assertThat(pagedResponse).isNotNull();
        assertThat(pagedResponse.getPage()).isEqualTo(0);
        assertThat(pagedResponse.isFirst()).isTrue();
        assertThat(pagedResponse.getContent()).hasSize(1);
    }

    @Test
    void shouldGetSubsequentPage() {
        Pageable pageable = PageRequest.of(1, 10);
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 20);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
        when(productMapper.toResponse(product)).thenReturn(productResponse);

        PagedResponse<ProductResponse> pagedResponse = productService.getProducts(1, 10);

        assertThat(pagedResponse).isNotNull();
        assertThat(pagedResponse.getPage()).isEqualTo(1);
        assertThat(pagedResponse.isFirst()).isFalse();
    }

    @Test
    void shouldGetEmptyPage() {
        Pageable pageable = PageRequest.of(5, 10);
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

        PagedResponse<ProductResponse> pagedResponse = productService.getProducts(5, 10);

        assertThat(pagedResponse).isNotNull();
        assertThat(pagedResponse.getContent()).isEmpty();
        assertThat(pagedResponse.getTotalElements()).isEqualTo(0);
    }

    @Test
    void shouldHonorRequestedPageSize() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        productService.getProducts(0, 5);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
    }

    @Test
    void shouldCapMaximumPageSizeAt100() {
        Pageable pageable = PageRequest.of(0, 100);
        Page<Product> productPage = new PageImpl<>(List.of(product), pageable, 1);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        productService.getProducts(0, 150);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findAll(pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
    }

    @Test
    void shouldUpdateProductSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(securityUtil.getCurrentUsername()).thenReturn("admin");
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

        ProductResponse response = productService.updateProduct(1L, productRequest);

        assertThat(response).isNotNull();
        verify(productRepository, times(1)).save(product);
    }

    @Test
    void shouldDeleteProductSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(product);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistentProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
