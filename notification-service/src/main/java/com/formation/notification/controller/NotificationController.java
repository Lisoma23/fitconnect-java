package com.formation.notification.controller;

import com.formation.notification.dto.NotificationRequest;
import com.formation.notification.dto.NotificationResponse;
import com.formation.notification.model.Notification;
import com.formation.notification.repository.NotificationRepository;
import com.formation.notification.service.NotificationMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    public NotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody NotificationRequest request) {
        Notification notification = notificationRepository.save(NotificationMapper.toEntity(request));
        return NotificationMapper.toResponse(notification);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponse> findByUserId(@PathVariable Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }
}