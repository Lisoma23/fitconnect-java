package com.formation.payment.service;

import com.formation.payment.dto.PaymentRequest;
import com.formation.payment.dto.PaymentResponse;
import com.formation.payment.exception.PaymentAlreadyRefundedException;
import com.formation.payment.exception.PaymentNotFoundException;
import com.formation.payment.model.Payment;
import com.formation.payment.model.PaymentStatus;
import com.formation.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {

    private static final BigDecimal FAILURE_THRESHOLD = BigDecimal.valueOf(100);

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    public PaymentService(PaymentRepository paymentRepository, PaymentMapper paymentMapper) {
        this.paymentRepository = paymentRepository;
        this.paymentMapper = paymentMapper;
    }

    @Transactional
    public PaymentResponse process(PaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        payment.setPaymentReference(generatePaymentReference());
        payment.setPaymentDate(LocalDateTime.now());

        if (isSimulatedSuccess(request.getAmount())) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setTransactionId(generateTransactionId());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
        }

        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Transactional
    public PaymentResponse refund(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException(id));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new PaymentAlreadyRefundedException(id);
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        Payment saved = paymentRepository.save(payment);
        return paymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByBookingId(Long bookingId) {
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByUserId(Long userId) {
        return paymentRepository.findByUserId(userId).stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
    }

    private boolean isSimulatedSuccess(BigDecimal amount) {
        return amount.compareTo(FAILURE_THRESHOLD) < 0;
    }

    private String generatePaymentReference() {
        String suffix = UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
        return "PAY-" + suffix;
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}