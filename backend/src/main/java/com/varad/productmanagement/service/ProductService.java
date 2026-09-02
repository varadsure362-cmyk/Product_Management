package com.varad.productmanagement.service;

import com.varad.productmanagement.dto.common.PagedResponse;
import com.varad.productmanagement.dto.product.ProductRequest;
import com.varad.productmanagement.dto.product.ProductResponse;

public interface ProductService {

    PagedResponse<ProductResponse> getProducts(int page, int size);

    ProductResponse getProductById(Long id);

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);
}
