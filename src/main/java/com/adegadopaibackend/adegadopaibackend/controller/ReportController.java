package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateReportRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.ReportResponse;
import com.adegadopaibackend.adegadopaibackend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportResponse> create(@Valid @RequestBody CreateReportRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<ReportResponse>> findAll() {
        return ResponseEntity.ok(reportService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.findById(id));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<ReportResponse>> findByType(@PathVariable String type) {
        return ResponseEntity.ok(reportService.findByType(type));
    }
}
