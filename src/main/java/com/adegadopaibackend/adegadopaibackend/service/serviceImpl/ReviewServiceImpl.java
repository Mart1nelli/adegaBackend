package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateReviewRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.ReviewResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Product;
import com.adegadopaibackend.adegadopaibackend.entity.Review;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.mapper.ReviewMapper;
import com.adegadopaibackend.adegadopaibackend.repository.ProductRepository;
import com.adegadopaibackend.adegadopaibackend.repository.ReviewRepository;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import com.adegadopaibackend.adegadopaibackend.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public ReviewResponse create(Long productId, CreateReviewRequest req) {
        Long userId = securityUtils.getAuthenticatedUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with ID: " + productId + " not found"));

        Review review = reviewMapper.toEntity(req);
        review.setUser(user);
        review.setProduct(product);

        return reviewMapper.toResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> findMyReviews() {
        return findByUserId(securityUtils.getAuthenticatedUserId());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // A segurança (isOwner ou ADMIN) é garantida pelo @PreAuthorize no Controller
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review with ID: " + id + " not found"));
        review.setIsActive(false);
        reviewRepository.save(review);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> findByProductId(Long productId) {
        return reviewMapper.toResponseList(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> findByUserId(Long userId) {
        return reviewMapper.toResponseList(reviewRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }
}
