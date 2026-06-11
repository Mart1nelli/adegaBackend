package com.adegadopaibackend.adegadopaibackend.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SecurityUtils {

    public Authentication getAuthentication() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User not authenticated");
        }
        return auth;
    }

    public UserPrincipal getAuthenticatedUser() {
        var principal = getAuthentication().getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal;
        }
        throw new AuthenticationCredentialsNotFoundException("User details not found");
    }

    public Long getAuthenticatedUserId() {
        return getAuthenticatedUser().id();
    }

    public boolean isAdmin() {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(a -> Objects.equals(a.getAuthority(), "ROLE_ADMIN"));
    }
}