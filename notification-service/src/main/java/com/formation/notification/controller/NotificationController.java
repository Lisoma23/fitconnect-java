package com.formation.notification.controller;

import com.formation.notification.dto.NotificationRequest;
import com.formation.notification.dto.NotificationResponse;
import com.formation.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(@Valid @RequestBody NotificationRequest request) {
        return notificationService.send(request);
    }

    @GetMapping("/user/{userId}")
    public List<NotificationResponse> findByUserId(@PathVariable Long userId) {
        return notificationService.findByUserId(userId);
    }

    @GetMapping("/pending")
    public List<NotificationResponse> findPending() {
        return notificationService.findPending();
    }

    @PatchMapping("/{id}/retry")
    public NotificationResponse retry(@PathVariable Long id) {
        return notificationService.retry(id);
    }
}