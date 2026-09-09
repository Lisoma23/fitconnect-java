package com.formation.booking.repository;

import com.formation.booking.model.Booking;
import com.formation.booking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByStatusAndPaymentDeadlineBefore(BookingStatus status, LocalDateTime deadline);

    List<Booking> findByStatusAndClassDateBetween(BookingStatus status, LocalDateTime from, LocalDateTime to);
}
