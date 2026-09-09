package com.formation.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookingRequest {

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    private Long userId;

    @NotBlank(message = "L'email de l'utilisateur est obligatoire")
    private String userEmail;

    @NotBlank(message = "Le nom de l'utilisateur est obligatoire")
    private String userName;

    @NotNull(message = "L'identifiant du cours est obligatoire")
    private Long classId;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Le nombre de places doit etre entre 1 et 4")
    @Max(value = 4, message = "Le nombre de places doit etre entre 1 et 4")
    private Integer numberOfSpots;

    public BookingRequest() {
    }

    public BookingRequest(Long userId, String userEmail, String userName, Long classId, Integer numberOfSpots) {
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.classId = classId;
        this.numberOfSpots = numberOfSpots;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getClassId() {
        return classId;
    }

    public void setClassId(Long classId) {
        this.classId = classId;
    }

    public Integer getNumberOfSpots() {
        return numberOfSpots;
    }

    public void setNumberOfSpots(Integer numberOfSpots) {
        this.numberOfSpots = numberOfSpots;
    }
}
