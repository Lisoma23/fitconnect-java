package com.formation.booking.client;

import com.formation.booking.dto.NotificationDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/notifications")
    NotificationDto send(@RequestBody NotificationRequest request);

    class NotificationRequest {
        private Long userId;
        private String email;
        private String type;
        private String subject;
        private String content;

        public NotificationRequest() {
        }

        public NotificationRequest(Long userId, String email, String type, String subject, String content) {
            this.userId = userId;
            this.email = email;
            this.type = type;
            this.subject = subject;
            this.content = content;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
