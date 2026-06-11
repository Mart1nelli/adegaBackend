package com.adegadopaibackend.adegadopaibackend.service.security;

import com.adegadopaibackend.adegadopaibackend.repository.NotificationRepository;
import com.adegadopaibackend.adegadopaibackend.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("notificationSecurity")
@RequiredArgsConstructor
public class NotificationSecurity {

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;

    public boolean isOwner(Long notificationId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        return notificationRepository.findById(notificationId)
                .map(notification -> notification.getUser().getId().equals(userId))
                .orElse(false);
    }
}
