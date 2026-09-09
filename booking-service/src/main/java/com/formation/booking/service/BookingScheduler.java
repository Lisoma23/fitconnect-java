package com.formation.booking.service;

import com.formation.booking.client.ClassClient;
import com.formation.booking.client.NotificationClient;
import com.formation.booking.exception.ServiceUnavailableException;
import com.formation.booking.model.Booking;
import com.formation.booking.model.BookingStatus;
import com.formation.booking.repository.BookingRepository;
import feign.FeignException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingScheduler {

    private final BookingRepository bookingRepository;
    private final ClassClient classClient;
    private final NotificationClient notificationClient;

    public BookingScheduler(BookingRepository bookingRepository, ClassClient classClient,
                            NotificationClient notificationClient) {
        this.bookingRepository = bookingRepository;
        this.classClient = classClient;
        this.notificationClient = notificationClient;
    }

    @Scheduled(fixedRate = 300000)
    @Transactional
    public void expirePendingPayments() {
        List<Booking> expired = bookingRepository
                .findByStatusAndPaymentDeadlineBefore(BookingStatus.PENDING_PAYMENT, LocalDateTime.now());

        for (Booking booking : expired) {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            decrementSpots(booking.getClassId());
            sendNotification(
                    booking.getUserId(),
                    booking.getUserEmail(),
                    "BOOKING_CANCELLED",
                    "Paiement expire - reservation annulee",
                    "Le delai de paiement de votre reservation pour le cours "
                            + booking.getClassName() + " est expire. La reservation a ete annulee."
            );
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void sendClassReminders() {
        LocalDateTime inTwentyFourHours = LocalDateTime.now().plusHours(24);
        List<Booking> upcoming = bookingRepository
                .findByStatusAndClassDateBetween(BookingStatus.CONFIRMED,
                        inTwentyFourHours.withMinute(0).withSecond(0).withNano(0),
                        inTwentyFourHours.withMinute(59).withSecond(59).withNano(999999999));

        for (Booking booking : upcoming) {
            sendNotification(
                    booking.getUserId(),
                    booking.getUserEmail(),
                    "BOOKING_REMINDER",
                    "Rappel de cours",
                    "Votre cours " + booking.getClassName() + " commence dans 24 heures ("
                            + booking.getClassDate() + ")."
            );
        }
    }

    private void decrementSpots(Long classId) {
        try {
            classClient.decrement(classId);
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
}
