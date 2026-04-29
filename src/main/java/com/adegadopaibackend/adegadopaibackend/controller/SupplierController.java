package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateSupplierRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateSupplierRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.SupplierResponse;
import com.adegadopaibackend.adegadopaibackend.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/suppliers")
@PreAuthorize("hasRole('ADMIN')")
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@Valid @RequestBody CreateSupplierRequest req) {
        SupplierResponse res = supplierService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> findAll() {
        List<SupplierResponse> res = supplierService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id,@Valid @RequestBody UpdateSupplierRequest req) {
        return  ResponseEntity.ok(supplierService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        supplierService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
