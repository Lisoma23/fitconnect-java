package com.formation.classes.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.formation.classes.model.Category;
import com.formation.classes.model.ClassStatus;
import com.formation.classes.model.Level;

public class FitnessClassResponse {
    private Long id;
    private String name;
    private String description;
    private String instructor;
    private String gymLocation;
    private Category category;
    private Level level;
    private Integer durationMinutes;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal price;
    private LocalDateTime dateTime;
    private ClassStatus status;
    private Long version;

    public FitnessClassResponse(Long id, String name, String description, String instructor, String gymLocation,
            Category category, Level level, Integer durationMinutes, Integer maxParticipants,
            Integer currentParticipants, BigDecimal price, LocalDateTime dateTime, ClassStatus status,
            Long version) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.instructor = instructor;
        this.gymLocation = gymLocation;
        this.category = category;
        this.level = level;
        this.durationMinutes = durationMinutes;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
        this.price = price;
        this.dateTime = dateTime;
        this.status = status;
        this.version = version;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getGymLocation() {
        return gymLocation;
    }

    public Category getCategory() {
        return category;
    }

    public Level getLevel() {
        return level;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public ClassStatus getStatus() {
        return status;
    }

    public Long getVersion() {
        return version;
    }
}