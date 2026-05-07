package com.fitness.gym.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class ClassEnrollmentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long memberId;
    private Long classId;

    @BeforeEach
    void setUp() throws Exception {
        String memberResponse = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "enroll-member@test.com", "fullName": "Enrollment Member"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        memberId = objectMapper.readTree(memberResponse).get("memberId").asLong();

        String trainerResponse = mockMvc.perform(post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName": "Test Trainer"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long trainerId = objectMapper.readTree(trainerResponse).get("trainerId").asLong();

        String roomResponse = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test Room", "maxCapacity": 10}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long roomId = objectMapper.readTree(roomResponse).get("roomId").asLong();

        String classResponse = mockMvc.perform(post("/api/gym-classes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "trainerId": %d,
                                  "roomId": %d,
                                  "title": "Yoga Class",
                                  "startTime": "2026-07-01T10:00:00",
                                  "endTime": "2026-07-01T11:00:00",
                                  "maxParticipants": 10
                                }
                                """, trainerId, roomId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        classId = objectMapper.readTree(classResponse).get("classId").asLong();
    }

    @Test
    void createEnrollment_returns201_withEnrollmentData() throws Exception {
        mockMvc.perform(post("/api/class-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"memberId": %d, "classId": %d}
                                """, memberId, classId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enrollmentId").isNumber())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.classId").value(classId));
    }

    @Test
    void createDuplicateEnrollment_returns409() throws Exception {
        mockMvc.perform(post("/api/class-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"memberId": %d, "classId": %d}
                                """, memberId, classId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/class-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"memberId": %d, "classId": %d}
                                """, memberId, classId)))
                .andExpect(status().isConflict());
    }

    @Test
    void createEnrollment_withInactiveMember_returns400() throws Exception {
        String inactiveResponse = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "inactive@test.com", "fullName": "Inactive Member"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long inactiveMemberId = objectMapper.readTree(inactiveResponse).get("memberId").asLong();

        mockMvc.perform(delete("/api/members/{id}", inactiveMemberId))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/class-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"memberId": %d, "classId": %d}
                                """, inactiveMemberId, classId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEnrollmentById_returns200_afterCreate() throws Exception {
        String response = mockMvc.perform(post("/api/class-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"memberId": %d, "classId": %d}
                                """, memberId, classId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long enrollmentId = objectMapper.readTree(response).get("enrollmentId").asLong();

        mockMvc.perform(get("/api/class-enrollments/{id}", enrollmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enrollmentId").value(enrollmentId));
    }

    @Test
    void getAllEnrollments_returns200() throws Exception {
        mockMvc.perform(post("/api/class-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"memberId": %d, "classId": %d}
                                """, memberId, classId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/class-enrollments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deleteEnrollment_returns204() throws Exception {
        String response = mockMvc.perform(post("/api/class-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"memberId": %d, "classId": %d}
                                """, memberId, classId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long enrollmentId = objectMapper.readTree(response).get("enrollmentId").asLong();

        mockMvc.perform(delete("/api/class-enrollments/{id}", enrollmentId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/class-enrollments/{id}", enrollmentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEnrollment_withNonExistentMember_returns404() throws Exception {
        mockMvc.perform(post("/api/class-enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {"memberId": 99999, "classId": %d}
                                """, classId)))
                .andExpect(status().isNotFound());
    }
}
