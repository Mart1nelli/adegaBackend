package com.adegadopaibackend.adegadopaibackend.service.security;

import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("userSecurity")
@RequiredArgsConstructor
public class UserSecurity {

    private final SecurityUtils securityUtils;

    public boolean canAccessUser(Long userId) {
        if (securityUtils.isAdmin()) {
            return true;
        }
        return securityUtils.getAuthenticatedUserId().equals(userId);
    }
}