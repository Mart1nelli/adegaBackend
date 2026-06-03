package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateProductRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateProductRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaginatedResponse;
import com.adegadopaibackend.adegadopaibackend.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(CreateProductRequest req);

    PaginatedResponse<ProductResponse> findAll(int page, int limit);

    ProductResponse findById(Long id);

    ProductResponse update(Long id, UpdateProductRequest req);

    void delete(Long id);
}
