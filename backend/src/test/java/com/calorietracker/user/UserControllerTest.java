package com.calorietracker.user;

import com.calorietracker.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class UserControllerTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String token;
    private String email;

    @BeforeEach
    void setup() throws Exception {
        email = UUID.randomUUID() + "@example.com";
        String regResponse = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "s3cr3t!!",
                                "displayName", "Ann"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode regJson = objectMapper.readTree(regResponse);
        token = regJson.get("accessToken").asText();
    }

    @Test
    void getMe_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMe_returnsProfile() throws Exception {
        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.displayName").value("Ann"))
                .andExpect(jsonPath("$.dailyKcalGoal").doesNotExist());
    }

    @Test
    void putMe_updatesDailyKcalGoal() throws Exception {
        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("dailyKcalGoal", 2200))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyKcalGoal").value(2200));

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailyKcalGoal").value(2200));
    }

    @Test
    void putMe_withNonPositiveGoal_returns400() throws Exception {
        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("dailyKcalGoal", -100))))
                .andExpect(status().isBadRequest());
    }
}
