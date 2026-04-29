package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateCategoryRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateCategoryRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.CategoryResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Category;
import com.adegadopaibackend.adegadopaibackend.mapper.CategoryMapper;
import com.adegadopaibackend.adegadopaibackend.repository.CategoryRepository;
import com.adegadopaibackend.adegadopaibackend.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService{

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse create(CreateCategoryRequest req) {

        if (categoryRepository.existsByName(req.getName())) {
            throw new IllegalArgumentException("Category already exists");
        }

        Category category = categoryMapper.toEntity(req);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public List<CategoryResponse> findAll() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }

    @Override
    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category  with ID: " + id + " not found"));
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponse update(Long id, UpdateCategoryRequest req) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category  with ID: " + id + " not found"));

        if (categoryRepository.existsByNameAndIdNot(req.getName(), id)) {
            throw new IllegalArgumentException("Category already exists");
        }

        categoryMapper.updateEntity(req, category);
        Category updatedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category  with ID: " + id + " not found"));
        category.setIsActive(false);
        categoryRepository.save(category);
    }
}
