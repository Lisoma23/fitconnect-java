package com.formation.booking.service;

import com.formation.booking.client.ClassClient;
import com.formation.booking.client.NotificationClient;
import com.formation.booking.model.Booking;
import com.formation.booking.model.BookingStatus;
import com.formation.booking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingSchedulerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ClassClient classClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private BookingScheduler bookingScheduler;

    private Booking buildBooking(BookingStatus status, LocalDateTime bookingDate, LocalDateTime classDate) {
        Booking booking = new Booking(
                "BK-12345", 1L, "john@example.com", "John Doe", 1L,
                "Yoga du matin", classDate, "Marie",
                new BigDecimal("15.00"), 2, new BigDecimal("30.00"),
                bookingDate, status,
                bookingDate.plusHours(1), classDate.minusHours(24)
        );
        booking.setId(1L);
        return booking;
    }

    @Test
    void expirePendingPayments_annuleEtLibereLesPlaces() {
        Booking expired = buildBooking(BookingStatus.PENDING_PAYMENT,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().plusDays(2));
        when(bookingRepository.findByStatusAndPaymentDeadlineBefore(
                org.mockito.ArgumentMatchers.eq(BookingStatus.PENDING_PAYMENT),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(expired));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        bookingScheduler.expirePendingPayments();

        assertThat(expired.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(classClient).decrement(1L);
        verify(notificationClient).send(any(NotificationClient.NotificationRequest.class));
    }

    @Test
    void expirePendingPayments_aucunBookingExpire_rienNeSePasse() {
        when(bookingRepository.findByStatusAndPaymentDeadlineBefore(
                org.mockito.ArgumentMatchers.eq(BookingStatus.PENDING_PAYMENT),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());

        bookingScheduler.expirePendingPayments();

        verify(classClient, never()).decrement(anyLong());
        verify(notificationClient, never()).send(any());
    }

    @Test
    void sendClassReminders_notifieLesCoursDans24Heures() {
        Booking upcoming = buildBooking(BookingStatus.CONFIRMED,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(24));
        when(bookingRepository.findByStatusAndClassDateBetween(
                org.mockito.ArgumentMatchers.eq(BookingStatus.CONFIRMED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of(upcoming));

        bookingScheduler.sendClassReminders();

        verify(notificationClient, times(1)).send(any(NotificationClient.NotificationRequest.class));
    }

    @Test
    void sendClassReminders_aucunCoursProche_rienNeSePasse() {
        when(bookingRepository.findByStatusAndClassDateBetween(
                org.mockito.ArgumentMatchers.eq(BookingStatus.CONFIRMED),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class),
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(List.of());

        bookingScheduler.sendClassReminders();

        verify(notificationClient, never()).send(any());
    }
}
