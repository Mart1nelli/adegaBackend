package com.adegadopaibackend.adegadopaibackend.service.security;

import com.adegadopaibackend.adegadopaibackend.repository.AddressRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("addressSecurity")
@RequiredArgsConstructor
public class AddressSecurity {

    private final AddressRepository addressRepository;
    private final SecurityUtils securityUtils;

    public boolean isOwner(Long addressId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return addressRepository.findById(addressId)
                .map(address -> address.getUser().getId().equals(userId))
                .orElse(false);
    }
}
