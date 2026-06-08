package com.calorietracker.food;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class FoodControllerTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void authenticate() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String response = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "s3cr3t!!"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        token = objectMapper.readTree(response).get("accessToken").asText();
    }

    // ── search ──────────────────────────────────────────────────────────────

    @Test
    void search_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/foods/search").param("q", "apple"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void search_returnsMatchingFoods() throws Exception {
        String uniqueName = "TestFood-" + UUID.randomUUID();
        createFood(uniqueName, 100.0);

        mockMvc.perform(get("/foods/search")
                        .param("q", uniqueName)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value(uniqueName))
                .andExpect(jsonPath("$[0].energyKcal").value(100.0));
    }

    @Test
    void search_returnsEmptyArrayForNoMatches() throws Exception {
        mockMvc.perform(get("/foods/search")
                        .param("q", "zzz-no-such-food-" + UUID.randomUUID())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_isCaseInsensitive() throws Exception {
        String base = "Mango-" + UUID.randomUUID();
        createFood(base.toLowerCase(), 60.0);

        mockMvc.perform(get("/foods/search")
                        .param("q", base.toUpperCase())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(base.toLowerCase()));
    }

    // ── get by id ────────────────────────────────────────────────────────────

    @Test
    void getById_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/foods/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_returnsFood() throws Exception {
        long id = createFoodAndGetId("Solo-" + UUID.randomUUID(), 250.0);

        mockMvc.perform(get("/foods/{id}", id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.energyKcal").value(250.0))
                .andExpect(jsonPath("$.source").value("USER"));
    }

    @Test
    void getById_forUnknownId_returns404() throws Exception {
        mockMvc.perform(get("/foods/{id}", Long.MAX_VALUE)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void createFood_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Granola bar",
                                "energyKcal", 420
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createFood_returnsCreatedFoodWithUserSource() throws Exception {
        String name = "Granola-" + UUID.randomUUID();

        mockMvc.perform(post("/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "energyKcal", 420,
                                "proteinG", 8.5,
                                "carbsG", 58.0,
                                "fatG", 16.0
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.energyKcal").value(420.0))
                .andExpect(jsonPath("$.proteinG").value(8.5))
                .andExpect(jsonPath("$.source").value("USER"))
                .andExpect(jsonPath("$.type").value("PACKAGED"));
    }

    @Test
    void createFood_missingEnergyKcal_returns400() throws Exception {
        mockMvc.perform(post("/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Incomplete food"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createFood_missingName_returns400() throws Exception {
        mockMvc.perform(post("/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "energyKcal", 100
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createdFood_isReturnedBySearch() throws Exception {
        String name = "Searchable-" + UUID.randomUUID();
        createFood(name, 300.0);

        mockMvc.perform(get("/foods/search")
                        .param("q", name)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(name));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void createFood(String name, double energyKcal) throws Exception {
        mockMvc.perform(post("/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "energyKcal", energyKcal
                        ))))
                .andExpect(status().isCreated());
    }

    private long createFoodAndGetId(String name, double energyKcal) throws Exception {
        String response = mockMvc.perform(post("/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name,
                                "energyKcal", energyKcal
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("id").asLong();
    }
}
