package com.varad.productmanagement.mapper;

import com.varad.productmanagement.dto.product.ProductRequest;
import com.varad.productmanagement.dto.product.ProductResponse;
import com.varad.productmanagement.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request, String createdBy) {
        return Product.builder()
                .productName(request.getProductName())
                .createdBy(createdBy)
                .build();
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .productName(product.getProductName())
                .createdBy(product.getCreatedBy())
                .createdOn(product.getCreatedOn())
                .modifiedBy(product.getModifiedBy())
                .modifiedOn(product.getModifiedOn())
                .build();
    }
}
