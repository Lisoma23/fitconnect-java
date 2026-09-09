package com.formation.classes.service;

import com.formation.classes.dto.FitnessClassRequest;
import com.formation.classes.dto.FitnessClassResponse;
import com.formation.classes.model.FitnessClass;

public final class FitnessClassMapper {

    private FitnessClassMapper() {
    }

    public static FitnessClass toEntity(FitnessClassRequest request) {
        return new FitnessClass(request.getName(), request.getDescription(), request.getInstructor(),
                request.getGymLocation(), request.getCategory(), request.getLevel(), request.getDurationMinutes(),
                request.getMaxParticipants(), request.getCurrentParticipants(), request.getPrice(),
                request.getDateTime(), request.getStatus());
    }

    public static FitnessClassResponse toResponse(FitnessClass fitnessClass) {
        return new FitnessClassResponse(fitnessClass.getId(), fitnessClass.getName(), fitnessClass.getDescription(),
                fitnessClass.getInstructor(), fitnessClass.getGymLocation(), fitnessClass.getCategory(),
                fitnessClass.getLevel(), fitnessClass.getDurationMinutes(), fitnessClass.getMaxParticipants(),
                fitnessClass.getCurrentParticipants(), fitnessClass.getPrice(), fitnessClass.getDateTime(),
                fitnessClass.getStatus(), fitnessClass.getVersion());
    }
}