package com.formation.booking.client;

import com.formation.booking.dto.PaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/payments")
    PaymentDto processPayment(@RequestBody PaymentRequest request);

    @GetMapping("/api/payments/booking/{bookingId}")
    List<PaymentDto> findByBookingId(@PathVariable("bookingId") Long bookingId);

    @RequestMapping(method = RequestMethod.POST, value = "/api/payments/{id}/refund")
    PaymentDto refund(@PathVariable("id") Long id);

    class PaymentRequest {
        private Long bookingId;
        private String bookingReference;
        private Long userId;
        private BigDecimal amount;
        private String paymentMethod;
        private String cardLastFour;
        private String transactionId;

        public PaymentRequest() {
        }

        public PaymentRequest(Long bookingId, String bookingReference, Long userId, BigDecimal amount,
                              String paymentMethod, String cardLastFour, String transactionId) {
            this.bookingId = bookingId;
            this.bookingReference = bookingReference;
            this.userId = userId;
            this.amount = amount;
            this.paymentMethod = paymentMethod;
            this.cardLastFour = cardLastFour;
            this.transactionId = transactionId;
        }

        public Long getBookingId() {
            return bookingId;
        }

        public void setBookingId(Long bookingId) {
            this.bookingId = bookingId;
        }

        public String getBookingReference() {
            return bookingReference;
        }

        public void setBookingReference(String bookingReference) {
            this.bookingReference = bookingReference;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public String getCardLastFour() {
            return cardLastFour;
        }

        public void setCardLastFour(String cardLastFour) {
            this.cardLastFour = cardLastFour;
        }

        public String getTransactionId() {
            return transactionId;
        }

        public void setTransactionId(String transactionId) {
            this.transactionId = transactionId;
        }
    }
}
