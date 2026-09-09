package com.formation.classes.controller;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.formation.classes.dto.FitnessClassRequest;
import com.formation.classes.dto.FitnessClassResponse;
import com.formation.classes.model.Category;
import com.formation.classes.model.Level;
import com.formation.classes.service.FitnessClassService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/classes")
public class FitnessClassController {

    private final FitnessClassService fitnessClassService;

    public FitnessClassController(FitnessClassService fitnessClassService) {
        this.fitnessClassService = fitnessClassService;
    }

    @GetMapping
    public Page<FitnessClassResponse> findAll(@RequestParam(required = false) Category category,
            @RequestParam(required = false) Level level, @RequestParam(required = false) String location,
            @PageableDefault Pageable pageable) {
        return fitnessClassService.findAll(category, level, location, null, null, pageable);
    }

    @GetMapping("/search")
    public Page<FitnessClassResponse> search(@RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Category category, @RequestParam(required = false) Level level,
            @RequestParam(required = false) String location, @PageableDefault Pageable pageable) {
        return fitnessClassService.search(category, level, location,
                date == null ? null : date.atStartOfDay(),
                date == null ? null : date.plusDays(1).atStartOfDay(), pageable);
    }

    @GetMapping("/{id}")
    public FitnessClassResponse findById(@PathVariable Long id) {
        return fitnessClassService.findById(id);
    }

    @PostMapping
    public ResponseEntity<FitnessClassResponse> create(@Valid @RequestBody FitnessClassRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fitnessClassService.create(request));
    }

    @RequestMapping (value = "/{id}", method = RequestMethod.PUT)
    public FitnessClassResponse update(@PathVariable Long id, @Valid @RequestBody FitnessClassRequest request) {
        return fitnessClassService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        fitnessClassService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/increment")
    public FitnessClassResponse incrementParticipants(@PathVariable Long id,
            @RequestParam int spots) {
        return fitnessClassService.incrementParticipants(id, spots);
    }

    @PatchMapping("/{id}/decrement")
    public FitnessClassResponse decrementParticipants(@PathVariable Long id) {
        return fitnessClassService.decrementParticipants(id);
    }
}