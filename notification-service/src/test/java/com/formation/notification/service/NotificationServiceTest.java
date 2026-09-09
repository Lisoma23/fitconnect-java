package com.formation.notification.service;

import com.formation.notification.dto.NotificationRequest;
import com.formation.notification.dto.NotificationResponse;
import com.formation.notification.exception.NotificationNotFoundException;
import com.formation.notification.model.Notification;
import com.formation.notification.model.NotificationStatus;
import com.formation.notification.model.NotificationType;
import com.formation.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void sendCreatesNotificationWithSentStatus() {
        NotificationRequest request = validRequest();
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationResponse response = notificationService.send(request);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(captor.getValue().getSentDate()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    @Test
    void retryFailedNotificationChangesStatusToSent() {
        Notification notification = notification(NotificationStatus.FAILED);
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        NotificationResponse response = notificationService.retry(1L);

        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(notification.getSentDate()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.SENT);
        verify(notificationRepository).save(notification);
    }

    @Test
    void findByUserIdWithoutNotificationsReturnsEmptyList() {
        when(notificationRepository.findByUserId(99L)).thenReturn(List.of());

        List<NotificationResponse> responses = notificationService.findByUserId(99L);

        assertThat(responses).isEmpty();
    }

    private NotificationRequest validRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setEmail("user@example.com");
        request.setType(NotificationType.BOOKING_CONFIRMATION);
        request.setSubject("Booking confirmation");
        request.setContent("Your booking has been confirmed.");
        request.setStatus(NotificationStatus.PENDING);
        return request;
    }

    private Notification notification(NotificationStatus status) {
        return new Notification(1L, "user@example.com", NotificationType.BOOKING_CONFIRMATION,
                "Booking confirmation", "Your booking has been confirmed.", status);
    }
}