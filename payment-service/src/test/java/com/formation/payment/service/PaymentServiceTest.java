package com.formation.payment.service;

import com.formation.payment.dto.PaymentRequest;
import com.formation.payment.dto.PaymentResponse;
import com.formation.payment.exception.PaymentAlreadyRefundedException;
import com.formation.payment.exception.PaymentNotFoundException;
import com.formation.payment.model.Payment;
import com.formation.payment.model.PaymentMethod;
import com.formation.payment.model.PaymentStatus;
import com.formation.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
        paymentService = new PaymentService(paymentRepository, paymentMapper);
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
    void process_shouldReturnSuccess_whenAmountBelow100() {
        PaymentRequest request = buildRequest(BigDecimal.valueOf(50));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment p = invocation.getArgument(0);
                    p.setId(1L);
                    return p;
                });

        PaymentResponse response = paymentService.process(request);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.SUCCESS);
        assertThat(response.getPaymentReference()).startsWith("PAY-");

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void process_shouldReturnFailed_whenAmountAtOrAbove100() {
        PaymentRequest request = buildRequest(BigDecimal.valueOf(100));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> {
                    Payment p = invocation.getArgument(0);
                    p.setId(2L);
                    return p;
                });

        PaymentResponse response = paymentService.process(request);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(response.getPaymentReference()).startsWith("PAY-");
    }

    @Test
    void refund_shouldSetStatusToRefunded_whenPaymentExists() {
        Payment existing = new Payment();
        existing.setId(1L);
        existing.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response = paymentService.refund(1L);

        assertThat(response.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
        verify(paymentRepository).save(existing);
    }

    @Test
    void refund_shouldThrow_whenAlreadyRefunded() {
        Payment existing = new Payment();
        existing.setId(1L);
        existing.setStatus(PaymentStatus.REFUNDED);

        when(paymentRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> paymentService.refund(1L))
                .isInstanceOf(PaymentAlreadyRefundedException.class);

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void refund_shouldThrow_whenPaymentNotFound() {
        when(paymentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.refund(99L))
                .isInstanceOf(PaymentNotFoundException.class);
    }

    @Test
    void findByBookingId_shouldReturnEmptyList_whenNoPaymentsExist() {
        when(paymentRepository.findByBookingId(404L)).thenReturn(Optional.empty());

        List<PaymentResponse> results = paymentService.findByBookingId(404L);

        assertThat(results).isEmpty();
    }
    @Test
    void findByBookingId_shouldReturnPayment_whenPaymentExists() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setBookingId(1L);
        payment.setStatus(PaymentStatus.SUCCESS);

        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));

        List<PaymentResponse> results = paymentService.findByBookingId(1L);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getId()).isEqualTo(1L);
    }
}