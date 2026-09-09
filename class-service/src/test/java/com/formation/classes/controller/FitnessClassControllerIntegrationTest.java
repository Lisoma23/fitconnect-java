package com.formation.classes.controller;

import com.formation.classes.dto.FitnessClassRequest;
import com.formation.classes.model.Category;
import com.formation.classes.model.ClassStatus;
import com.formation.classes.model.FitnessClass;
import com.formation.classes.model.Level;
import com.formation.classes.repository.FitnessClassRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FitnessClassControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FitnessClassRepository fitnessClassRepository;

    @BeforeEach
    void cleanDatabase() {
        fitnessClassRepository.deleteAll();
    }

    private FitnessClassRequest buildRequest(int maxParticipants, int currentParticipants) {
        FitnessClassRequest request = new FitnessClassRequest();
        request.setName("Yoga du matin");
        request.setDescription("Séance de yoga douce");
        request.setInstructor("Marie");
        request.setGymLocation("Paris");
        request.setCategory(Category.YOGA);
        request.setLevel(Level.BEGINNER);
        request.setDurationMinutes(60);
        request.setMaxParticipants(maxParticipants);
        request.setCurrentParticipants(currentParticipants);
        request.setPrice(new BigDecimal("15.00"));
        request.setDateTime(LocalDateTime.now().plusDays(3));
        request.setStatus(ClassStatus.SCHEDULED);
        return request;
    }

    @Test
    void cycleDeVieComplet_creerListerRecupererModifierSupprimer() throws Exception {
        String response = mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest(10, 0))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Yoga du matin"))
                .andExpect(jsonPath("$.currentParticipants").value(0))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        mockMvc.perform(get("/api/classes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga du matin"));

        FitnessClassRequest update = buildRequest(20, 0);
        update.setName("Yoga avancé");
        mockMvc.perform(put("/api/classes/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Yoga avancé"));

        mockMvc.perform(delete("/api/classes/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/classes/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void increment_surUnCoursPlein_retourne409() throws Exception {
        FitnessClass fitnessClass = new FitnessClass("Yoga", "Cours", "Coach", "Paris",
                Category.YOGA, Level.BEGINNER, 60, 10, 10, BigDecimal.TEN,
                LocalDateTime.now().plusDays(1), ClassStatus.SCHEDULED);
        fitnessClassRepository.save(fitnessClass);

        mockMvc.perform(patch("/api/classes/{id}/increment", fitnessClass.getId())
                        .param("spots", "1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("No spots available for fitness class: " + fitnessClass.getId()));
    }

    @Test
    void increment_nominal_retourne200() throws Exception {
        FitnessClass fitnessClass = new FitnessClass("Yoga", "Cours", "Coach", "Paris",
                Category.YOGA, Level.BEGINNER, 60, 10, 5, BigDecimal.TEN,
                LocalDateTime.now().plusDays(1), ClassStatus.SCHEDULED);
        fitnessClassRepository.save(fitnessClass);

        mockMvc.perform(patch("/api/classes/{id}/increment", fitnessClass.getId())
                        .param("spots", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentParticipants").value(7));
    }

    @Test
    void coursInexistant_retourne404() throws Exception {
        mockMvc.perform(get("/api/classes/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void validationInvalide_retourne400() throws Exception {
        FitnessClassRequest request = buildRequest(10, 0);
        request.setName("Yo");

        mockMvc.perform(post("/api/classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void filtresEtPagination_fonctionnent() throws Exception {
        FitnessClass yoga = new FitnessClass("Yoga", "Cours", "Marie", "Paris",
                Category.YOGA, Level.BEGINNER, 60, 10, 0, BigDecimal.TEN,
                LocalDateTime.now().plusDays(1), ClassStatus.SCHEDULED);
        FitnessClass crossfit = new FitnessClass("Crossfit", "Cours", "Paul", "Lyon",
                Category.CROSSFIT, Level.ADVANCED, 60, 10, 0, BigDecimal.TEN,
                LocalDateTime.now().plusDays(2), ClassStatus.SCHEDULED);
        fitnessClassRepository.save(yoga);
        fitnessClassRepository.save(crossfit);

        mockMvc.perform(get("/api/classes")
                        .param("category", "YOGA")
                        .param("level", "BEGINNER")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Yoga"));
    }
}
