package com.adegadopaibackend.adegadopaibackend.service.security;

import com.adegadopaibackend.adegadopaibackend.repository.ReviewRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("reviewSecurity")
@RequiredArgsConstructor
public class ReviewSecurity {

    private final ReviewRepository reviewRepository;
    private final SecurityUtils securityUtils;

    public boolean isOwner(Long reviewId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return reviewRepository.findById(reviewId)
                .map(review -> review.getUser().getId().equals(userId))
                .orElse(false);
    }
}
