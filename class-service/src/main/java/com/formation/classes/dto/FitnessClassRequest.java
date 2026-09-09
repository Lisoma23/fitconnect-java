package com.formation.classes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.formation.classes.model.Category;
import com.formation.classes.model.ClassStatus;
import com.formation.classes.model.Level;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class FitnessClassRequest {

    @NotBlank
    @Size(min = 3)
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String instructor;

    @NotBlank
    private String gymLocation;

    @NotNull
    private Category category;

    @NotNull
    private Level level;

    @NotNull
    @Min(30)
    private Integer durationMinutes;

    @NotNull
    @Min(5)
    @Max(30)
    private Integer maxParticipants;

    @NotNull
    @Min(0)
    private Integer currentParticipants;

    @NotNull
    @DecimalMin("5.00")
    private BigDecimal price;

    @NotNull
    @Future
    private LocalDateTime dateTime;

    @NotNull
    private ClassStatus status;

    public FitnessClassRequest() {
    }

    @AssertTrue(message = "Duration must be 30, 45, 60, or 90 minutes")
    public boolean isDurationAllowed() {
        return durationMinutes == null || durationMinutes == 30 || durationMinutes == 45
                || durationMinutes == 60 || durationMinutes == 90;
    }

    @AssertTrue(message = "Current participants cannot exceed max participants")
    public boolean isParticipantCountValid() {
        return currentParticipants == null || maxParticipants == null
                || currentParticipants <= maxParticipants;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getGymLocation() {
        return gymLocation;
    }

    public void setGymLocation(String gymLocation) {
        this.gymLocation = gymLocation;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public void setCurrentParticipants(Integer currentParticipants) {
        this.currentParticipants = currentParticipants;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public ClassStatus getStatus() {
        return status;
    }

    public void setStatus(ClassStatus status) {
        this.status = status;
    }
}