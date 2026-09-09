package com.formation.booking.controller;

import com.formation.booking.dto.BookingConfirmRequest;
import com.formation.booking.dto.BookingRequest;
import com.formation.booking.dto.BookingResponse;
import com.formation.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<BookingResponse> getAll() {
        return bookingService.findAll();
    }

    @GetMapping("/{id}")
    public BookingResponse getById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<BookingResponse> getByUserId(@PathVariable Long userId) {
        return bookingService.findByUserId(userId);
    }

    @GetMapping("/expired")
    public List<BookingResponse> getExpired() {
        return bookingService.findExpired();
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        BookingResponse created = bookingService.create(request);
        return ResponseEntity.created(URI.create("/api/bookings/" + created.getId())).body(created);
    }

    @PatchMapping("/{id}/confirm")
    public BookingResponse confirm(@PathVariable Long id, @Valid @RequestBody BookingConfirmRequest request) {
        return bookingService.confirm(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }

    @PatchMapping("/{id}/complete")
    public BookingResponse complete(@PathVariable Long id) {
        return bookingService.complete(id);
    }
}
