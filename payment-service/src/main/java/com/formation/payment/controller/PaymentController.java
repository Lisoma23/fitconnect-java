package com.formation.payment.controller;

import com.formation.payment.dto.PaymentRequest;
import com.formation.payment.dto.PaymentResponse;
import com.formation.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> process(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.process(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<PaymentResponse> refund(@PathVariable Long id) {
        PaymentResponse response = paymentService.refund(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<List<PaymentResponse>> findByBookingId(@PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.findByBookingId(bookingId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentService.findByUserId(userId));
    }
}