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

    // --- Rotas do Usuário ---

    @PostMapping("/product/{productId}")
    public ResponseEntity<ReviewResponse> create(@PathVariable Long productId,
                                                 @Valid @RequestBody CreateReviewRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(productId, req));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ReviewResponse>> findMyReviews() {
        return ResponseEntity.ok(reviewService.findMyReviews());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@reviewSecurity.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Público ---

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponse>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.findByProductId(productId));
    }

    // --- Admin ---

    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.findByUserId(userId));
    }
}
