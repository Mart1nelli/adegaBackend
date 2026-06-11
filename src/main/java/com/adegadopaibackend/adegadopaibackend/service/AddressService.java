package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateAddressRequest;
import com.adegadopaibackend.adegadopaibackend.dto.request.UpdateAddressRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    // Rotas do usuário (userId extraído do token via SecurityUtils)
    AddressResponse create(CreateAddressRequest req);
    List<AddressResponse> findMyAddresses();
    AddressResponse findById(Long id);
    AddressResponse update(Long id, UpdateAddressRequest req);
    void delete(Long id);

    // Rotas do Admin (userId explícito da URL)
    AddressResponse createForAdmin(Long userId, CreateAddressRequest req);
    List<AddressResponse> findAllByUserId(Long userId);
}
