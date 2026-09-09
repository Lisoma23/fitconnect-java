package com.fitconnect.paymentservice.repository;

import com.fitconnect.paymentservice.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByBookingId(Long bookingId);

    List<Payment> findByUserId(Long userId);

    Optional<Payment> findByPaymentReference(String paymentReference);
}