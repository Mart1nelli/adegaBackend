package com.adegadopaibackend.adegadopaibackend.controller;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateAddressRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateAddressRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.AddressResponse;
import com.adegadopaibackend.adegadopaibackend.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;

    // --- Rotas do Usuário ---

    @PostMapping
    public ResponseEntity<AddressResponse> create(@Valid @RequestBody CreateAddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.create(req));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> findMyAddresses() {
        return ResponseEntity.ok(addressService.findMyAddresses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@addressSecurity.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@addressSecurity.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateAddressRequest req) {
        return ResponseEntity.ok(addressService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@addressSecurity.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // --- Rotas do Admin ---

    @PostMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AddressResponse> createForAdmin(@PathVariable Long userId,
                                                          @Valid @RequestBody CreateAddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(addressService.createForAdmin(userId, req));
    }

    @GetMapping("/admin/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AddressResponse>> findAllByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.findAllByUserId(userId));
    }
}
