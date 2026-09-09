package com.formation.classes.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.formation.classes.model.Category;
import com.formation.classes.model.FitnessClass;
import com.formation.classes.model.Level;

public interface FitnessClassRepository extends JpaRepository<FitnessClass, Long> {
    List<FitnessClass> findByCategory(Category category);

    List<FitnessClass> findByLevel(Level level);

    List<FitnessClass> findByGymLocationContainingIgnoreCase(String gymLocation);

    List<FitnessClass> findByInstructorContainingIgnoreCase(String instructor);

    List<FitnessClass> findByDateTimeBetween(LocalDateTime start, LocalDateTime end);
}