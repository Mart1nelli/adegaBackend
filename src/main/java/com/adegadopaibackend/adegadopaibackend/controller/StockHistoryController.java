package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateStockHistoryRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.StockHistoryResponse;
import com.adegadopaibackend.adegadopaibackend.service.StockHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stock-history")
@PreAuthorize("hasRole('ADMIN')")
public class StockHistoryController {

    private final StockHistoryService stockHistoryService;

    @PostMapping("/product/{productId}")
    public ResponseEntity<StockHistoryResponse> create(@PathVariable Long productId,
                                                       @Valid @RequestBody CreateStockHistoryRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockHistoryService.create(productId, req));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<StockHistoryResponse>> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(stockHistoryService.findByProductId(productId));
    }
}
