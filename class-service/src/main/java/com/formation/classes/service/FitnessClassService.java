package com.formation.classes.service;

import java.time.LocalDateTime;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.formation.classes.dto.FitnessClassRequest;
import com.formation.classes.dto.FitnessClassResponse;
import com.formation.classes.exception.FitnessClassNotFoundException;
import com.formation.classes.exception.NoSpotsAvailableException;
import com.formation.classes.exception.OptimisticLockException;
import com.formation.classes.model.Category;
import com.formation.classes.model.ClassStatus;
import com.formation.classes.model.FitnessClass;
import com.formation.classes.model.Level;
import com.formation.classes.repository.FitnessClassRepository;

@Service
public class FitnessClassService {

    private final FitnessClassRepository fitnessClassRepository;

    public FitnessClassService(FitnessClassRepository fitnessClassRepository) {
        this.fitnessClassRepository = fitnessClassRepository;
    }

    @Transactional(readOnly = true)
    public Page<FitnessClassResponse> findAll(Pageable pageable) {
        return findAll(null, null, null, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FitnessClassResponse> findAll(Category category, Level level, String location,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return search(category, level, location, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public Page<FitnessClassResponse> search(Category category, Level level, String location,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<FitnessClass> specification = Specification.where(null);
        if (category != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("category"), category));
        }
        if (level != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("level"), level));
        }
        if (location != null && !location.isBlank()) {
            specification = specification.and((root, query, builder) -> builder.like(
                    builder.lower(root.get("gymLocation")), "%" + location.toLowerCase() + "%"));
        }
        if (from != null) {
            specification = specification
                    .and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("dateTime"), from));
        }
        if (to != null) {
            specification = specification
                    .and((root, query, builder) -> builder.lessThanOrEqualTo(root.get("dateTime"), to));
        }
        return fitnessClassRepository.findAll(specification, pageable).map(FitnessClassMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public FitnessClassResponse findById(Long id) {
        return FitnessClassMapper.toResponse(getClassOrThrow(id));
    }

    @Transactional
    public FitnessClassResponse create(FitnessClassRequest request) {
        FitnessClass fitnessClass = FitnessClassMapper.toEntity(request);
        fitnessClass.setCurrentParticipants(0);
        return save(fitnessClass);
    }

    @Transactional
    public FitnessClassResponse update(Long id, FitnessClassRequest request) {
        FitnessClass fitnessClass = getClassOrThrow(id);
        fitnessClass.setName(request.getName());
        fitnessClass.setDescription(request.getDescription());
        fitnessClass.setInstructor(request.getInstructor());
        fitnessClass.setGymLocation(request.getGymLocation());
        fitnessClass.setCategory(request.getCategory());
        fitnessClass.setLevel(request.getLevel());
        fitnessClass.setDurationMinutes(request.getDurationMinutes());
        fitnessClass.setMaxParticipants(request.getMaxParticipants());
        fitnessClass.setCurrentParticipants(request.getCurrentParticipants());
        fitnessClass.setPrice(request.getPrice());
        fitnessClass.setStatus(request.getStatus());
        fitnessClass.setDateTime(request.getDateTime());
        return save(fitnessClass);
    }

    @Transactional
    public void delete(Long id) {
        FitnessClass fitnessClass = getClassOrThrow(id);
        fitnessClass.setStatus(ClassStatus.CANCELLED);
        save(fitnessClass);
    }

    @Transactional
    public FitnessClassResponse incrementParticipants(Long id, int spots) {
        if (spots <= 0) {
            throw new IllegalArgumentException("spots must be greater than zero");
        }
        FitnessClass fitnessClass = getClassOrThrow(id);
        if (fitnessClass.getCurrentParticipants() + spots > fitnessClass.getMaxParticipants()) {
            throw new NoSpotsAvailableException(id);
        }
        fitnessClass.setCurrentParticipants(fitnessClass.getCurrentParticipants() + spots);
        return save(fitnessClass);
    }

    @Transactional
    public FitnessClassResponse decrementParticipants(Long id) {
        FitnessClass fitnessClass = getClassOrThrow(id);
        fitnessClass.setCurrentParticipants(Math.max(0, fitnessClass.getCurrentParticipants() - 1));
        return save(fitnessClass);
    }

    private FitnessClassResponse save(FitnessClass fitnessClass) {
        try {
            return FitnessClassMapper.toResponse(fitnessClassRepository.save(fitnessClass));
        } catch (OptimisticLockingFailureException exception) {
            throw new OptimisticLockException(fitnessClass.getId());
        }
    }

    private FitnessClass getClassOrThrow(Long id) {
        return fitnessClassRepository.findById(id)
                .orElseThrow(() -> new FitnessClassNotFoundException(id));
    }
}