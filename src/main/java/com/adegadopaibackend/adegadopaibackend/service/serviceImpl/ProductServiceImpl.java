package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateProductRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateProductRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.PaginatedResponse;
import com.adegadopaibackend.adegadopaibackend.dto.response.ProductResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Category;
import com.adegadopaibackend.adegadopaibackend.entity.Product;
import com.adegadopaibackend.adegadopaibackend.entity.Supplier;
import com.adegadopaibackend.adegadopaibackend.mapper.ProductMapper;
import com.adegadopaibackend.adegadopaibackend.repository.CategoryRepository;
import com.adegadopaibackend.adegadopaibackend.repository.ProductRepository;
import com.adegadopaibackend.adegadopaibackend.repository.SupplierRepository;
import com.adegadopaibackend.adegadopaibackend.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Override
    public ProductResponse create(CreateProductRequest req) {
        Product product = productMapper.toEntity(req);

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        product.setCategory(category);

        if(req.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(req.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));

            product.setSupplier(supplier);
        }

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<ProductResponse> findAll(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Product> productPage = productRepository.findAll(pageable); // Use o Page do Spring Data

        List<ProductResponse> dtos = productMapper.toResponseList(productPage.getContent());

        return PaginatedResponse.<ProductResponse>builder()
                .data(dtos)
                .total(productPage.getTotalElements())
                .page(page)
                .limit(limit)
                .totalPages(productPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse update(Long id, UpdateProductRequest req) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        productMapper.updateEntity(req, product);

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        product.setCategory(category);

        if (req.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(req.getSupplierId())
                    .orElseThrow(() -> new EntityNotFoundException("Supplier not found"));
            product.setSupplier(supplier);
        } else {
            product.setSupplier(null);
        }

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        product.setIsActive(false);
        productRepository.save(product);
    }
}
