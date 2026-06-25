package com.adegadopaibackend.adegadopaibackend.service;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateNotificationRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    // Rotas do Admin (userId explícito)
    NotificationResponse create(Long userId, CreateNotificationRequest req);
    List<NotificationResponse> findByUserId(Long userId);

    // Rotas do Usuário (userId extraído do token)
    List<NotificationResponse> findMyNotifications();

    // Shared (proteção por @PreAuthorize no Controller)
    NotificationResponse markAsRead(Long id);
    void delete(Long id);
}
