package com.fitness.gym.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class MemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createMember_returns201_withMemberData() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "integration@test.com",
                                  "fullName": "Integration User",
                                  "phone": "0700123456",
                                  "dateOfBirth": "1990-05-15"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").isNumber())
                .andExpect(jsonPath("$.email").value("integration@test.com"))
                .andExpect(jsonPath("$.fullName").value("Integration User"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void createMember_returns409_whenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "dup@test.com", "fullName": "First User"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "dup@test.com", "fullName": "Second User"}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void getMemberById_returns200_afterCreate() throws Exception {
        String response = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "getbyid@test.com", "fullName": "Get By Id User"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long memberId = objectMapper.readTree(response).get("memberId").asLong();

        mockMvc.perform(get("/api/members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.email").value("getbyid@test.com"));
    }

    @Test
    void getMemberById_returns404_whenNotFound() throws Exception {
        mockMvc.perform(get("/api/members/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllMembers_returns200_withList() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "list1@test.com", "fullName": "List User One"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateMember_returns200_withUpdatedData() throws Exception {
        String response = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "update@test.com", "fullName": "Original Name"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long memberId = objectMapper.readTree(response).get("memberId").asLong();

        mockMvc.perform(put("/api/members/{id}", memberId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "updated@test.com", "fullName": "Updated Name"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@test.com"))
                .andExpect(jsonPath("$.fullName").value("Updated Name"));
    }

    @Test
    void deactivateMember_returns204() throws Exception {
        String response = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "deactivate@test.com", "fullName": "To Deactivate"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long memberId = objectMapper.readTree(response).get("memberId").asLong();

        mockMvc.perform(delete("/api/members/{id}", memberId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/members/{id}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
