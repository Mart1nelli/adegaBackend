package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateReviewRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.ReviewResponse;
import com.adegadopaibackend.adegadopaibackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/user/{userId}/product/{productId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<ReviewResponse> create(@PathVariable Long userId,
                                                 @PathVariable Long productId,
                                                 @Valid @RequestBody CreateReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(userId, productId, req));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.findByProductId(productId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<List<ReviewResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.findByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
