package com.formation.notification.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.formation.notification.dto.NotificationRequest;
import com.formation.notification.dto.NotificationResponse;
import com.formation.notification.exception.NotificationNotFoundException;
import com.formation.notification.model.Notification;
import com.formation.notification.model.NotificationStatus;
import com.formation.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationResponse send(NotificationRequest request) {
        Notification notification = NotificationMapper.toEntity(request);
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentDate(LocalDateTime.now());
        return NotificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findByUserId(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findPending() {
        return notificationRepository.findByStatus(NotificationStatus.PENDING).stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse retry(Long id) {
        Notification notification = getNotificationOrThrow(id);
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentDate(LocalDateTime.now());
        return NotificationMapper.toResponse(notificationRepository.save(notification));
    }

    private Notification getNotificationOrThrow(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException(id));
    }
}