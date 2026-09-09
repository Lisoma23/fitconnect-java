package com.formation.booking.controller;

import com.formation.booking.client.ClassClient;
import com.formation.booking.client.NotificationClient;
import com.formation.booking.client.PaymentClient;
import com.formation.booking.dto.BookingConfirmRequest;
import com.formation.booking.dto.BookingRequest;
import com.formation.booking.dto.ClassDto;
import com.formation.booking.dto.PaymentDto;
import com.formation.booking.repository.BookingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @MockBean
    private ClassClient classClient;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private NotificationClient notificationClient;

    @BeforeEach
    void cleanDatabase() {
        bookingRepository.deleteAll();
    }

    private ClassDto buildClass(int current, int max, BigDecimal price, LocalDateTime dateTime) {
        ClassDto dto = new ClassDto();
        dto.setId(1L);
        dto.setName("Yoga du matin");
        dto.setInstructor("Marie");
        dto.setGymLocation("Paris");
        dto.setCategory("YOGA");
        dto.setLevel("BEGINNER");
        dto.setMaxParticipants(max);
        dto.setCurrentParticipants(current);
        dto.setPrice(price);
        dto.setDateTime(dateTime);
        return dto;
    }

    @Test
    void sagaComplet_creerBookerConfirmer() throws Exception {
        ClassDto fitnessClass = buildClass(5, 10, new BigDecimal("15.00"), LocalDateTime.now().plusDays(3));
        when(classClient.getClassById(eq(1L))).thenReturn(fitnessClass);
        doNothing().when(classClient).increment(eq(1L), eq(2));
        when(notificationClient.send(any())).thenReturn(null);

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 1L, 2);

        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.totalAmount").value(30.0))
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(response).get("id").asLong();

        PaymentDto payment = new PaymentDto();
        payment.setStatus("SUCCESS");
        when(paymentClient.processPayment(any())).thenReturn(payment);

        BookingConfirmRequest confirmRequest = new BookingConfirmRequest("CREDIT_CARD", "1234", "txn_123");

        mockMvc.perform(patch("/api/bookings/{id}/confirm", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(classClient).increment(1L, 2);
        verify(paymentClient).processPayment(any());
        verify(notificationClient, org.mockito.Mockito.times(2)).send(any());
    }

    @Test
    void create_surReservation_retourne409() throws Exception {
        ClassDto fitnessClass = buildClass(9, 10, new BigDecimal("15.00"), LocalDateTime.now().plusDays(3));
        when(classClient.getClassById(eq(1L))).thenReturn(fitnessClass);

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 1L, 2);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Plus de places disponibles pour ce cours (id 1)"));
    }

    @Test
    void create_coursInexistant_retourne400() throws Exception {
        when(classClient.getClassById(eq(999L))).thenThrow(notFoundException());

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 999L, 1);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Le cours 999 demande dans la reservation est introuvable"));
    }

    @Test
    void create_casConcurrence_increment409_retourne409() throws Exception {
        ClassDto fitnessClass = buildClass(5, 10, new BigDecimal("15.00"), LocalDateTime.now().plusDays(3));
        when(classClient.getClassById(eq(1L))).thenReturn(fitnessClass);
        doThrow(conflictException()).when(classClient).increment(eq(1L), eq(2));

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 1L, 2);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void confirm_paiementExpire_retourne409() throws Exception {
        ClassDto fitnessClass = buildClass(5, 10, new BigDecimal("15.00"), LocalDateTime.now().plusDays(3));
        when(classClient.getClassById(eq(1L))).thenReturn(fitnessClass);
        doNothing().when(classClient).increment(eq(1L), eq(1));
        when(notificationClient.send(any())).thenReturn(null);

        BookingRequest request = new BookingRequest(1L, "john@example.com", "John Doe", 1L, 1);
        String response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long bookingId = objectMapper.readTree(response).get("id").asLong();

        com.formation.booking.model.Booking persisted = bookingRepository.findById(bookingId).orElseThrow();
        persisted.setPaymentDeadline(java.time.LocalDateTime.now().minusMinutes(5));
        bookingRepository.save(persisted);

        BookingConfirmRequest confirmRequest = new BookingConfirmRequest("CREDIT_CARD", "1234", "txn_123");

        mockMvc.perform(patch("/api/bookings/{id}/confirm", bookingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_validationInvalide_retourne400() throws Exception {
        BookingRequest request = new BookingRequest(1L, "", "", 1L, 5);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.userEmail").exists());
    }

    private static FeignException.NotFound notFoundException() {
        feign.Request request = feign.Request.create(feign.Request.HttpMethod.GET,
                "http://class-service/api/classes/999", java.util.Map.of(), null,
                feign.Util.UTF_8, null);
        feign.Response response = feign.Response.builder()
                .status(404)
                .reason("Not Found")
                .request(request)
                .body(new byte[0])
                .build();
        return (FeignException.NotFound) FeignException.errorStatus("GET /api/classes/999", response);
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
