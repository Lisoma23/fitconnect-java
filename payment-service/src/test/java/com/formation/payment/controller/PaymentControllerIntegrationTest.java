package com.formation.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.payment.dto.PaymentRequest;
import com.formation.payment.model.PaymentMethod;
import com.formation.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @AfterEach
    void cleanUp() {
        paymentRepository.deleteAll();
    }

    private PaymentRequest buildRequest(BigDecimal amount) {
        PaymentRequest request = new PaymentRequest();
        request.setBookingId(1L);
        request.setUserId(10L);
        request.setAmount(amount);
        request.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        request.setCardLastFour("4242");
        return request;
    }

    @Test
    void process_shouldReturn201AndSuccess_whenAmountBelow100() throws Exception {
        PaymentRequest request = buildRequest(BigDecimal.valueOf(50));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andExpect(jsonPath("$.paymentReference", startsWith("PAY-")));
    }

    @Test
    void process_shouldReturn201AndFailed_whenAmountAtOrAbove100() throws Exception {
        PaymentRequest request = buildRequest(BigDecimal.valueOf(150));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("FAILED")));
    }

    @Test
    void process_shouldReturn400_whenAmountIsMissing() throws Exception {
        PaymentRequest request = buildRequest(null);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.details", not(empty())));
    }

    @Test
    void refund_shouldReturn404_whenPaymentDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/payments/{id}/refund", 9999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    void fullLifecycle_createRetrieveRefund() throws Exception {
        PaymentRequest request = buildRequest(BigDecimal.valueOf(30));

        String createResponse = mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SUCCESS")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long paymentId = objectMapper.readTree(createResponse).get("id").asLong();

        mockMvc.perform(get("/api/payments/booking/{bookingId}", request.getBookingId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(paymentId.intValue())));

        mockMvc.perform(get("/api/payments/user/{userId}", request.getUserId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(paymentId.intValue())));

        mockMvc.perform(post("/api/payments/{id}/refund", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REFUNDED")));

        mockMvc.perform(post("/api/payments/{id}/refund", paymentId))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }
}