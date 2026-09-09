package com.formation.classes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.formation.classes.model.Category;
import com.formation.classes.model.ClassStatus;
import com.formation.classes.model.Level;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FitnessClassRequestValidationTest {

    @Autowired
    private Validator validator;

    @Test
    void acceptsValidRequest() {
        Set<ConstraintViolation<FitnessClassRequest>> violations = validator.validate(validRequest());

        assertThat(violations).isEmpty();
    }

    @Test
    void rejectsMissingRequiredFields() {
        Set<ConstraintViolation<FitnessClassRequest>> violations = validator.validate(new FitnessClassRequest());

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "description", "instructor", "gymLocation", "category", "level",
                        "durationMinutes", "maxParticipants", "currentParticipants", "price", "dateTime",
                        "status");
    }

    @Test
    void rejectsNameShorterThanThreeCharacters() {
        FitnessClassRequest request = validRequest();
        request.setName("ab");

        assertThat(propertyNames(request)).contains("name");
    }

    @Test
    void rejectsDurationOutsideAllowedValues() {
        FitnessClassRequest request = validRequest();
        request.setDurationMinutes(50);

        assertThat(propertyNames(request)).contains("durationAllowed");
    }

    @Test
    void rejectsInvalidParticipantCounts() {
        FitnessClassRequest request = validRequest();
        request.setMaxParticipants(4);

        assertThat(propertyNames(request)).contains("maxParticipants");

        request = validRequest();
        request.setCurrentParticipants(31);

        assertThat(propertyNames(request)).contains("participantCountValid");
    }

    @Test
    void rejectsPriceBelowFive() {
        FitnessClassRequest request = validRequest();
        request.setPrice(new BigDecimal("4.99"));

        assertThat(propertyNames(request)).contains("price");
    }

    @Test
    void rejectsDateTimeInThePast() {
        FitnessClassRequest request = validRequest();
        request.setDateTime(LocalDateTime.now().minusMinutes(1));

        assertThat(propertyNames(request)).contains("dateTime");
    }

    private FitnessClassRequest validRequest() {
        FitnessClassRequest request = new FitnessClassRequest();
        request.setName("Morning Yoga");
        request.setDescription("A relaxing yoga class.");
        request.setInstructor("Alex Martin");
        request.setGymLocation("Studio A");
        request.setCategory(Category.YOGA);
        request.setLevel(Level.BEGINNER);
        request.setDurationMinutes(60);
        request.setMaxParticipants(20);
        request.setCurrentParticipants(0);
        request.setPrice(new BigDecimal("12.50"));
        request.setDateTime(LocalDateTime.now().plusDays(1));
        request.setStatus(ClassStatus.SCHEDULED);
        return request;
    }

    private Set<String> propertyNames(FitnessClassRequest request) {
        return validator.validate(request).stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(java.util.stream.Collectors.toSet());
    }
}