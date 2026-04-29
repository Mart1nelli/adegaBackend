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
import com.adegadopaibackend.adegadopaibackend.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewMapper reviewMapper;

    private void verifyOwnership(Review review) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!loggedUser.getRole().equals("ADMIN") && !review.getUser().getId().equals(loggedUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this review.");
        }
    }

    @Override
    public ReviewResponse create(Long userId, Long productId, CreateReviewRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product with ID: " + productId + " not found"));

        Review review = reviewMapper.toEntity(req);
        review.setUser(user);
        review.setProduct(product);

        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toResponse(savedReview);
    }

    @Override
    public List<ReviewResponse> findByProductId(Long productId) {
        return reviewMapper.toResponseList(reviewRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @Override
    public List<ReviewResponse> findByUserId(Long userId) {
        return reviewMapper.toResponseList(reviewRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    public void delete(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review with ID: " + id + " not found"));
        verifyOwnership(review);
        review.setIsActive(false);
        reviewRepository.save(review);
    }
}
