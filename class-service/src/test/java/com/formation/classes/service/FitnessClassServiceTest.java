package com.formation.classes.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import com.formation.classes.dto.FitnessClassRequest;
import com.formation.classes.exception.NoSpotsAvailableException;
import com.formation.classes.exception.OptimisticLockException;
import com.formation.classes.exception.FitnessClassNotFoundException;
import com.formation.classes.model.Category;
import com.formation.classes.model.ClassStatus;
import com.formation.classes.model.FitnessClass;
import com.formation.classes.model.Level;
import com.formation.classes.repository.FitnessClassRepository;

@ExtendWith(MockitoExtension.class)
class FitnessClassServiceTest {

    @Mock
    private FitnessClassRepository fitnessClassRepository;

    @Test
    void incrementSurUnCoursPleinLeveUneExceptionSansSauvegarder() {
        FitnessClass fitnessClass = fitnessClass(10, 10);
        when(fitnessClassRepository.findById(1L)).thenReturn(Optional.of(fitnessClass));
        FitnessClassService service = new FitnessClassService(fitnessClassRepository);

        assertThatThrownBy(() -> service.incrementParticipants(1L, 1))
                .isInstanceOf(NoSpotsAvailableException.class);
        verify(fitnessClassRepository, never()).save(any());
    }

    @Test
    void decrementNePasseJamaisSousZero() {
        FitnessClass fitnessClass = fitnessClass(10, 0);
        when(fitnessClassRepository.findById(1L)).thenReturn(Optional.of(fitnessClass));
        when(fitnessClassRepository.save(fitnessClass)).thenReturn(fitnessClass);
        FitnessClassService service = new FitnessClassService(fitnessClassRepository);

        service.decrementParticipants(1L);

        assertThat(fitnessClass.getCurrentParticipants()).isZero();
    }

    @Test
    void createForceLeNombreDeParticipantsAZero() {
        FitnessClassRequest request = new FitnessClassRequest();
        request.setName("Yoga");
        request.setDescription("Cours");
        request.setInstructor("Coach");
        request.setGymLocation("Paris");
        request.setCategory(Category.YOGA);
        request.setLevel(Level.BEGINNER);
        request.setDurationMinutes(60);
        request.setMaxParticipants(10);
        request.setCurrentParticipants(8);
        request.setPrice(BigDecimal.TEN);
        request.setDateTime(LocalDateTime.now().plusDays(1));
        request.setStatus(ClassStatus.SCHEDULED);
        when(fitnessClassRepository.save(any(FitnessClass.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        FitnessClassService service = new FitnessClassService(fitnessClassRepository);

        service.create(request);

        ArgumentCaptor<FitnessClass> captor = ArgumentCaptor.forClass(FitnessClass.class);
        verify(fitnessClassRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentParticipants()).isZero();
    }

    @Test
    void conflitOptimisteEstConvertiEnExceptionMetier() {
        FitnessClass fitnessClass = fitnessClass(10, 1);
        when(fitnessClassRepository.findById(1L)).thenReturn(Optional.of(fitnessClass));
        when(fitnessClassRepository.save(fitnessClass))
                .thenThrow(new ObjectOptimisticLockingFailureException(FitnessClass.class, 1L));
        FitnessClassService service = new FitnessClassService(fitnessClassRepository);

        assertThatThrownBy(() -> service.incrementParticipants(1L, 1))
                .isInstanceOf(OptimisticLockException.class);
    }

    @Test
    void coursInexistantRetourneUne404() {
        when(fitnessClassRepository.findById(1L)).thenReturn(Optional.empty());
        FitnessClassService service = new FitnessClassService(fitnessClassRepository);

        assertThatThrownBy(() -> service.incrementParticipants(1L, 1))
                .isInstanceOf(FitnessClassNotFoundException.class);

    }

    private FitnessClass fitnessClass(int maxParticipants, int currentParticipants) {
        return new FitnessClass("Yoga", "Cours", "Coach", "Paris", Category.YOGA, Level.BEGINNER,
                60, maxParticipants, currentParticipants, BigDecimal.TEN,
                LocalDateTime.now().plusDays(1), ClassStatus.SCHEDULED);
    }
}
