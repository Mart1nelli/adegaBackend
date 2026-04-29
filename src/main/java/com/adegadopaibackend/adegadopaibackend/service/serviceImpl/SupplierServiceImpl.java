package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateSupplierRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateSupplierRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.SupplierResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Supplier;
import com.adegadopaibackend.adegadopaibackend.mapper.SupplierMapper;
import com.adegadopaibackend.adegadopaibackend.repository.SupplierRepository;
import com.adegadopaibackend.adegadopaibackend.service.SupplierService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Override
    public SupplierResponse create (CreateSupplierRequest req) {

        if(supplierRepository.existsByName(req.getName())) {
            throw new IllegalArgumentException("Supplier already exists");
        }

        Supplier supplier = supplierMapper.toEntity(req);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(savedSupplier);
    }

    @Override
    public List<SupplierResponse> findAll() {
        return supplierMapper.toResponseList(supplierRepository.findAll());
    }

    @Override
    public SupplierResponse findById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier with ID: " + id + " not found"));
        return supplierMapper.toResponse(supplier);
    }

    @Override
    public SupplierResponse update(Long id, UpdateSupplierRequest req) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier with ID: " + id + " not found"));

        if (supplierRepository.existsByNameAndIdNot(req.getName(), id)) {
            throw new IllegalArgumentException("Supplier already exists");
        }

        supplierMapper.updateEntity(req, supplier);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toResponse(updatedSupplier);
    }

    @Override
    public void delete(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Supplier with ID: " + id + " not found"));
        supplier.setIsActive(false);
        supplierRepository.save(supplier);
    }
}
