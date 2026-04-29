package com.adegadopaibackend.adegadopaibackend.service.serviceImpl;

import com.adegadopaibackend.adegadopaibackend.dto.request.CreateNotificationRequest;
import com.adegadopaibackend.adegadopaibackend.dto.response.NotificationResponse;
import com.adegadopaibackend.adegadopaibackend.entity.Notification;
import com.adegadopaibackend.adegadopaibackend.entity.User;
import com.adegadopaibackend.adegadopaibackend.mapper.NotificationMapper;
import com.adegadopaibackend.adegadopaibackend.repository.NotificationRepository;
import com.adegadopaibackend.adegadopaibackend.repository.UserRepository;
import com.adegadopaibackend.adegadopaibackend.service.NotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    private void verifyOwnership(Notification notification) {
        User loggedUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!loggedUser.getRole().equals("ADMIN") && !notification.getUser().getId().equals(loggedUser.getId())) {
            throw new AccessDeniedException("Access denied: You do not own this notification.");
        }
    }

    @Override
    public NotificationResponse create(Long userId, CreateNotificationRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User with ID: " + userId + " not found"));

        Notification notification = notificationMapper.toEntity(req);
        notification.setUser(user);

        Notification savedNotification = notificationRepository.save(notification);
        return notificationMapper.toResponse(savedNotification);
    }

    @Override
    public List<NotificationResponse> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User with ID: " + userId + " not found");
        }
        return notificationMapper.toResponseList(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification with ID: " + id + " not found"));

        verifyOwnership(notification);

        notification.setIsRead(true);
        Notification updated = notificationRepository.save(notification);
        return notificationMapper.toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notification with ID: " + id + " not found"));
        
        verifyOwnership(notification);
        
        notificationRepository.delete(notification);
    }
}
