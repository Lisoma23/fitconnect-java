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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ClassClient classClient;

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private NotificationClient notificationClient;

    @InjectMocks
    private BookingService bookingService;

    private ClassDto buildClass(int current, int max, BigDecimal price) {
        ClassDto dto = new ClassDto();
        dto.setId(1L);
        dto.setName("Yoga du matin");
        dto.setInstructor("Marie");
        dto.setDateTime(LocalDateTime.now().plusDays(3));
        dto.setMaxParticipants(max);
        dto.setCurrentParticipants(current);
        dto.setPrice(price);
        return dto;
    }

    private Booking buildBooking(BookingStatus status, LocalDateTime paymentDeadline, LocalDateTime cancellationDeadline) {
        Booking booking = new Booking(
                "BK-12345", 1L, "john@example.com", "John Doe", 1L,
                "Yoga du matin", LocalDateTime.now().plusDays(3), "Marie",
                new BigDecimal("15.00"), 2, new BigDecimal("30.00"),
                LocalDateTime.now(), status, paymentDeadline, cancellationDeadline
        );
        booking.setId(1L);
        return booking;
    }

    @Test
    void create_coursInexistant_leveClassNotFoundForBooking() {
        when(classClient.getClassById(99L)).thenThrow(notFoundException());

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 99L, 2);

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ClassNotFoundForBookingException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_plusDePlaces_leveNoSpotsEtNeReservePas() {
        ClassDto fitnessClass = buildClass(9, 10, new BigDecimal("15.00"));
        when(classClient.getClassById(1L)).thenReturn(fitnessClass);

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 1L, 2);

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(NoSpotsAvailableException.class)
                .hasMessageContaining("1");

        verify(classClient, never()).increment(anyLong(), anyInt());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_casDeConcurrence_classServiceRepond409_leveNoSpots() {
        ClassDto fitnessClass = buildClass(5, 10, new BigDecimal("15.00"));
        when(classClient.getClassById(1L)).thenReturn(fitnessClass);
        doThrow(conflictException()).when(classClient).increment(eq(1L), eq(2));

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 1L, 2);

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(NoSpotsAvailableException.class)
                .hasMessageContaining("1");

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void create_nominal_creerReservationPendingPayment() {
        ClassDto fitnessClass = buildClass(5, 10, new BigDecimal("15.00"));
        when(classClient.getClassById(1L)).thenReturn(fitnessClass);

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 1L, 2);

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(1L);
            return booking;
        });

        BookingResponse result = bookingService.create(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(result.getBookingReference()).startsWith("BK-");
        assertThat(result.getPaymentDeadline()).isEqualTo(result.getBookingDate().plusHours(1));
        assertThat(result.getCancellationDeadline()).isEqualTo(result.getClassDate().minusHours(24));
        assertThat(result.getClassName()).isEqualTo("Yoga du matin");

        verify(classClient).increment(1L, 2);
        verify(notificationClient).send(any(NotificationClient.NotificationRequest.class));
    }

    @Test
    void findById_empruntInexistant_leveBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(99L))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void confirm_paiementExpire_levePaymentExpired() {
        Booking booking = buildBooking(BookingStatus.PENDING_PAYMENT, LocalDateTime.now().minusMinutes(5),
                LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingConfirmRequest request = new BookingConfirmRequest("CREDIT_CARD", "1234", "txn_123");

        assertThatThrownBy(() -> bookingService.confirm(1L, request))
                .isInstanceOf(PaymentExpiredException.class)
                .hasMessageContaining("1");

        verify(paymentClient, never()).processPayment(any());
    }

    @Test
    void confirm_paiementReussi_statusConfirmed() {
        Booking booking = buildBooking(BookingStatus.PENDING_PAYMENT, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        PaymentDto payment = new PaymentDto();
        payment.setStatus("SUCCESS");
        when(paymentClient.processPayment(any(PaymentClient.PaymentRequest.class))).thenReturn(payment);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingConfirmRequest request = new BookingConfirmRequest("CREDIT_CARD", "1234", "txn_123");

        BookingResponse result = bookingService.confirm(1L, request);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(notificationClient).send(any(NotificationClient.NotificationRequest.class));
    }

    @Test
    void confirm_paiementRefuse_statusCancelled() {
        Booking booking = buildBooking(BookingStatus.PENDING_PAYMENT, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        PaymentDto payment = new PaymentDto();
        payment.setStatus("FAILED");
        when(paymentClient.processPayment(any(PaymentClient.PaymentRequest.class))).thenReturn(payment);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingConfirmRequest request = new BookingConfirmRequest("CREDIT_CARD", "1234", "txn_123");

        BookingResponse result = bookingService.confirm(1L, request);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void cancel_horsDelais_leveCancellationNotAllowed() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().minusHours(1));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancel(1L))
                .isInstanceOf(CancellationNotAllowedException.class)
                .hasMessageContaining("1");

        verify(paymentClient, never()).refund(anyLong());
        verify(classClient, never()).decrement(anyLong());
    }

    @Test
    void cancel_nominal_confirmed_refundEtDecrement() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        PaymentDto payment = new PaymentDto();
        payment.setStatus("REFUNDED");
        when(paymentClient.refund(1L)).thenReturn(payment);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse result = bookingService.cancel(1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        verify(paymentClient).refund(1L);
        verify(classClient).decrement(1L);
        verify(notificationClient).send(any(NotificationClient.NotificationRequest.class));
    }

    @Test
    void complete_dejaTermine_leveBookingAlreadyCompleted() {
        Booking booking = buildBooking(BookingStatus.COMPLETED, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.complete(1L))
                .isInstanceOf(BookingAlreadyCompletedException.class)
                .hasMessageContaining("1");
    }

    @Test
    void complete_nominal_statusCompleted() {
        Booking booking = buildBooking(BookingStatus.CONFIRMED, LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusDays(2));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponse result = bookingService.complete(1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void create_classServiceIndisponible_leveServiceUnavailable() {
        when(classClient.getClassById(1L)).thenThrow(new FeignException.InternalServerError(
                "boom", feign.Request.create(feign.Request.HttpMethod.GET,
                "http://class-service/api/classes/1", java.util.Map.of(), null,
                feign.Util.UTF_8, null), new byte[0], java.util.Map.of()));

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 1L, 2);

        assertThatThrownBy(() -> bookingService.create(request))
                .isInstanceOf(ServiceUnavailableException.class);
    }

    private static FeignException.NotFound notFoundException() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.GET,
                "http://class-service/api/classes/99", java.util.Map.of(), null,
                feign.Util.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .body(new byte[0])
                .build();
        return (FeignException.NotFound) FeignException.errorStatus("GET /api/classes/99", response);
    }

    private static FeignException.Conflict conflictException() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.PATCH,
                "http://class-service/api/classes/1/increment", java.util.Map.of(), null,
                feign.Util.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(409)
                .reason("Conflict")
                .request(request)
                .body(new byte[0])
                .build();
        return (FeignException.Conflict) FeignException.errorStatus("PATCH /api/classes/1/increment", response);
    }
}
