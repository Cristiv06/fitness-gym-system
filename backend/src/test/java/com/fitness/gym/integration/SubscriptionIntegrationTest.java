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
class SubscriptionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long memberId;
    private Long planId;

    @BeforeEach
    void setUp() throws Exception {
        String memberResponse = mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "sub-member@test.com", "fullName": "Subscription Member"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        memberId = objectMapper.readTree(memberResponse).get("memberId").asLong();

        String planResponse = mockMvc.perform(post("/api/membership-plans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Monthly Plan", "durationMonths": 1, "price": 99.99}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        planId = objectMapper.readTree(planResponse).get("planId").asLong();
    }

    @Test
    void createSubscription_returns201_withSubscriptionData() throws Exception {
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "memberId": %d,
                                  "planId": %d,
                                  "startDate": "2026-01-01",
                                  "endDate": "2026-01-31",
                                  "status": "ACTIVE"
                                }
                                """, memberId, planId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subscriptionId").isNumber())
                .andExpect(jsonPath("$.memberId").value(memberId))
                .andExpect(jsonPath("$.planId").value(planId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createSubscription_returns400_whenEndDateBeforeStartDate() throws Exception {
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "memberId": %d,
                                  "planId": %d,
                                  "startDate": "2026-06-01",
                                  "endDate": "2026-05-01"
                                }
                                """, memberId, planId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSubscriptionById_returns200_afterCreate() throws Exception {
        String response = mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "memberId": %d,
                                  "planId": %d,
                                  "startDate": "2026-03-01",
                                  "endDate": "2026-03-31"
                                }
                                """, memberId, planId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long subscriptionId = objectMapper.readTree(response).get("subscriptionId").asLong();

        mockMvc.perform(get("/api/subscriptions/{id}", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscriptionId").value(subscriptionId));
    }

    @Test
    void getAllSubscriptions_returns200() throws Exception {
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "memberId": %d,
                                  "planId": %d,
                                  "startDate": "2026-04-01",
                                  "endDate": "2026-04-30"
                                }
                                """, memberId, planId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/subscriptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deleteSubscription_returns204() throws Exception {
        String response = mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "memberId": %d,
                                  "planId": %d,
                                  "startDate": "2026-05-01",
                                  "endDate": "2026-05-31"
                                }
                                """, memberId, planId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long subscriptionId = objectMapper.readTree(response).get("subscriptionId").asLong();

        mockMvc.perform(delete("/api/subscriptions/{id}", subscriptionId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/subscriptions/{id}", subscriptionId))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSubscription_returns404_whenMemberNotFound() throws Exception {
        mockMvc.perform(post("/api/subscriptions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "memberId": 99999,
                                  "planId": %d,
                                  "startDate": "2026-01-01",
                                  "endDate": "2026-01-31"
                                }
                                """, planId)))
                .andExpect(status().isNotFound());
    }
}
