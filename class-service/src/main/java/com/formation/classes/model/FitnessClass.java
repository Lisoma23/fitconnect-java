package com.formation.classes.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "classes")
public class FitnessClass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private String instructor;
    private String gymLocation;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Enumerated(EnumType.STRING)
    private Level level;

    private Integer durationMinutes;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private BigDecimal price;
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    private ClassStatus status;

    @Version
    private Long version;

    protected FitnessClass() {
    }

    public FitnessClass(String name, String description, String instructor, String gymLocation,
            Category category, Level level, Integer durationMinutes, Integer maxParticipants,
            Integer currentParticipants, BigDecimal price, LocalDateTime dateTime, ClassStatus status) {
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
    }

    public Long getId() {
        return id;
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

    public void setDateTime() {
        this.dateTime = LocalDateTime.now();
    }

    public ClassStatus getStatus() {
        return status;
    }

    public void setStatus(ClassStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }
}