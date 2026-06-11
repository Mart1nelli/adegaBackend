package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateReviewRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {

    // Rotas do usuário
    ReviewResponse create(Long productId, CreateReviewRequest req);
    List<ReviewResponse> findMyReviews();
    void delete(Long id);

    // Público
    List<ReviewResponse> findByProductId(Long productId);

    // Admin
    List<ReviewResponse> findByUserId(Long userId);
}
