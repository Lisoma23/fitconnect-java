package com.formation.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.formation.notification.dto.NotificationRequest;
import com.formation.notification.model.Notification;
import com.formation.notification.model.NotificationStatus;
import com.formation.notification.model.NotificationType;
import com.formation.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void cleanDatabase() {
        notificationRepository.deleteAll();
    }

    @Test
    void lifecycleCreatesListsAndRetriesNotification() throws Exception {
        String response = mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("SENT")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/notifications/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("SENT")));

        mockMvc.perform(patch("/api/notifications/{id}/retry", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) id)))
                .andExpect(jsonPath("$.status", is("SENT")));
    }

    @Test
    void createReturns201() throws Exception {
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest()))
                .andExpect(status().isCreated());
    }

    @Test
    void findByUserIdReturns200() throws Exception {
        notificationRepository.save(notification(7L, NotificationStatus.SENT));

        mockMvc.perform(get("/api/notifications/user/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void retryReturns200() throws Exception {
        long id = notificationRepository.save(notification(1L, NotificationStatus.FAILED)).getId();

        mockMvc.perform(patch("/api/notifications/{id}/retry", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("SENT")));
    }

    @Test
    void invalidRequestReturns400() throws Exception {
        mockMvc.perform(post("/api/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1,\"email\":\"invalid-email\"}"))
                .andExpect(status().isBadRequest());
    }

    private String jsonRequest() throws Exception {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(1L);
        request.setEmail("user@example.com");
        request.setType(NotificationType.BOOKING_CONFIRMATION);
        request.setSubject("Booking confirmation");
        request.setContent("Your booking has been confirmed.");
        return objectMapper.writeValueAsString(request);
    }

    private Notification notification(Long userId, NotificationStatus status) {
        return new Notification(userId, "user@example.com", NotificationType.BOOKING_CONFIRMATION,
                "Booking confirmation", "Your booking has been confirmed.", status);
    }
}