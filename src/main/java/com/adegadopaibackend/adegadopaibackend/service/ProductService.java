package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateProductRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateProductRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaginatedResponse;
import com.adegadopaibackend.adegadopaibackend.dto.response.ProductResponse;

import java.math.BigDecimal; // IMPORTANTE: Adicione este import

public interface ProductService {

    ProductResponse create(CreateProductRequest req);

    PaginatedResponse<ProductResponse> findAll(int page, int limit, Long categoryId, String search, BigDecimal maxPrice);

    ProductResponse findById(Long id);

    ProductResponse update(Long id, UpdateProductRequest req);

    void delete(Long id);
}