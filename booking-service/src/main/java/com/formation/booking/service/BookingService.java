package com.formation.booking.service;

import com.formation.booking.client.ClassClient;
import com.formation.booking.client.NotificationClient;
import com.formation.booking.client.PaymentClient;
import com.formation.booking.dto.BookingConfirmRequest;
import com.formation.booking.dto.BookingRequest;
import com.formation.booking.dto.BookingResponse;
import com.formation.booking.dto.ClassDto;
import com.formation.booking.dto.PaymentDto;
import com.formation.booking.exception.BookingAlreadyCompletedException;
import com.formation.booking.exception.BookingNotFoundException;
import com.formation.booking.exception.CancellationNotAllowedException;
import com.formation.booking.exception.ClassNotFoundForBookingException;
import com.formation.booking.exception.NoSpotsAvailableException;
import com.formation.booking.exception.PaymentExpiredException;
import com.formation.booking.exception.ServiceUnavailableException;
import com.formation.booking.model.Booking;
import com.formation.booking.model.BookingStatus;
import com.formation.booking.repository.BookingRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final ClassClient classClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;

    public BookingService(BookingRepository bookingRepository, ClassClient classClient,
                          PaymentClient paymentClient, NotificationClient notificationClient) {
        this.bookingRepository = bookingRepository;
        this.classClient = classClient;
        this.paymentClient = paymentClient;
        this.notificationClient = notificationClient;
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findAll() {
        return bookingRepository.findAll().stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse findById(Long id) {
        return BookingMapper.toResponse(getBookingOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> findExpired() {
        return bookingRepository.findByStatusAndPaymentDeadlineBefore(BookingStatus.PENDING_PAYMENT, LocalDateTime.now())
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse create(BookingRequest request) {
        ClassDto fitnessClass = fetchClass(request.getClassId());

        if (fitnessClass.getCurrentParticipants() == null
                || fitnessClass.getMaxParticipants() == null
                || fitnessClass.getCurrentParticipants() + request.getNumberOfSpots() > fitnessClass.getMaxParticipants()) {
            throw new NoSpotsAvailableException(request.getClassId());
        }

        incrementSpots(request.getClassId(), request.getNumberOfSpots());

        LocalDateTime now = LocalDateTime.now();
        BigDecimal totalAmount = fitnessClass.getPrice().multiply(BigDecimal.valueOf(request.getNumberOfSpots()));

        Booking booking = new Booking(
                generateReference("BK-"),
                request.getUserId(),
                request.getUserEmail(),
                request.getUserName(),
                request.getClassId(),
                fitnessClass.getName(),
                fitnessClass.getDateTime(),
                fitnessClass.getInstructor(),
                fitnessClass.getPrice(),
                request.getNumberOfSpots(),
                totalAmount,
                now,
                BookingStatus.PENDING_PAYMENT,
                now.plusHours(1),
                fitnessClass.getDateTime().minusHours(24)
        );

        Booking saved = bookingRepository.save(booking);

        sendNotification(
                request.getUserId(),
                request.getUserEmail(),
                "BOOKING_CONFIRMATION",
                "Reservation en attente de paiement",
                "Votre reservation est en attente de paiement. Payez avant " + saved.getPaymentDeadline()
        );

        return BookingMapper.toResponse(saved);
    }

    @Transactional
    public BookingResponse confirm(Long id, BookingConfirmRequest request) {
        Booking booking = getBookingOrThrow(id);

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new PaymentExpiredException(id);
        }
        if (booking.getPaymentDeadline().isBefore(LocalDateTime.now())) {
            throw new PaymentExpiredException(id);
        }

        PaymentDto payment = processPayment(booking, request);

        if ("SUCCESS".equals(payment.getStatus())) {
            booking.setStatus(BookingStatus.CONFIRMED);
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
        }

        Booking saved = bookingRepository.save(booking);

        sendNotification(
                booking.getUserId(),
                booking.getUserEmail(),
                "PAYMENT_CONFIRMATION",
                "Paiement confirme",
                "Votre paiement a ete confirme pour le cours " + booking.getClassName()
        );

        return BookingMapper.toResponse(saved);
    }

    @Transactional
    public BookingResponse cancel(Long id) {
        Booking booking = getBookingOrThrow(id);

        if (booking.getStatus() == BookingStatus.CANCELLED || booking.getStatus() == BookingStatus.COMPLETED) {
            throw new CancellationNotAllowedException(id);
        }
        if (booking.getCancellationDeadline().isBefore(LocalDateTime.now())) {
            throw new CancellationNotAllowedException(id);
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            refundPayment(booking);
            decrementSpots(booking.getClassId());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        sendNotification(
                booking.getUserId(),
                booking.getUserEmail(),
                "BOOKING_CANCELLED",
                "Reservation annulee",
                "Votre reservation pour le cours " + booking.getClassName() + " a ete annulee"
        );

        return BookingMapper.toResponse(saved);
    }

    @Transactional
    public BookingResponse complete(Long id) {
        Booking booking = getBookingOrThrow(id);

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new BookingAlreadyCompletedException(id);
        }

        booking.setStatus(BookingStatus.COMPLETED);
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    private ClassDto fetchClass(Long classId) {
        try {
            return classClient.getClassById(classId);
        } catch (FeignException.NotFound ex) {
            throw new ClassNotFoundForBookingException(classId);
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }

    private void incrementSpots(Long classId, int spots) {
        try {
            classClient.increment(classId, spots);
        } catch (FeignException.Conflict ex) {
            throw new NoSpotsAvailableException(classId);
        } catch (FeignException.NotFound ex) {
            throw new ClassNotFoundForBookingException(classId);
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }

    private void decrementSpots(Long classId) {
        try {
            classClient.decrement(classId);
        } catch (FeignException.NotFound ex) {
            throw new ClassNotFoundForBookingException(classId);
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }

    private PaymentDto processPayment(Booking booking, BookingConfirmRequest request) {
        try {
            PaymentClient.PaymentRequest paymentRequest = new PaymentClient.PaymentRequest(
                    booking.getId(),
                    booking.getBookingReference(),
                    booking.getUserId(),
                    booking.getTotalAmount(),
                    request.getPaymentMethod(),
                    request.getCardLastFour(),
                    request.getTransactionId()
            );
            return paymentClient.processPayment(paymentRequest);
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }

    private void refundPayment(Booking booking) {
        try {
            PaymentDto payment = paymentClient.refund(booking.getId());
            if (payment == null) {
                throw new ServiceUnavailableException("payment-service n'a pas retourne de paiement");
            }
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }

    private void sendNotification(Long userId, String email, String type, String subject, String content) {
        try {
            NotificationClient.NotificationRequest request = new NotificationClient.NotificationRequest(
                    userId, email, type, subject, content
            );
            notificationClient.send(request);
        } catch (FeignException ex) {
            throw new ServiceUnavailableException(ex.getMessage());
        }
    }

    private Booking getBookingOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    private String generateReference(String prefix) {
        String suffix = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 5)
                .toUpperCase();
        return prefix + suffix;
    }
}
