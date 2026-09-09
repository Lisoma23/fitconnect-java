package com.formation.notification.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static com.formation.notification.model.NotificationType.BOOKING_CONFIRMATION;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NotificationRequestValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void acceptsValidRequest() {
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(validRequest());

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsInvalidEmail() {
        NotificationRequest request = validRequest();
        request.setEmail("email-invalide");

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email");
    }

    @Test
    void rejectsMissingRequiredFields() {
        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(new NotificationRequest());

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("userId", "email", "type", "subject", "content");
    }

    @Test
    void rejectsSubjectLongerThan255Characters() {
        NotificationRequest request = validRequest();
        request.setSubject("a".repeat(256));

        Set<ConstraintViolation<NotificationRequest>> violations = validator.validate(request);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("subject");
    }

    private NotificationRequest validRequest() {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setEmail("user@example.com");
        request.setType(BOOKING_CONFIRMATION);
        request.setSubject("Booking confirmation");
        request.setContent("Your booking has been confirmed.");
        return request;
    }
}