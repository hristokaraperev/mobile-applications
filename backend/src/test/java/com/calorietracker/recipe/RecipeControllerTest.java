package com.calorietracker.recipe;

import com.calorietracker.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class RecipeControllerTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String token;
    private Long foodId;

    @BeforeEach
    void setup() throws Exception {
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
        foodId = createFood("Apple", 52.0, 0.3, 14.0, 0.2);
    }

    // ── POST /recipes ────────────────────────────────────────────────────────

    @Test
    void createRecipe_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Apple Salad",
                                "numberOfPortions", 2,
                                "ingredients", List.of(Map.of("foodId", foodId, "grams", 200.0))
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRecipe_computesTotalAndPerPortionNutrition() throws Exception {
        // 200g of Apple (52 kcal, 0.3g protein, 14g carbs, 0.2g fat per 100g), split into 2 portions.
        // Total: kcal=104, protein=0.6, carbs=28, fat=0.4
        // Per portion: kcal=52, protein=0.3, carbs=14, fat=0.2
        String response = mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Apple Salad",
                                "numberOfPortions", 2,
                                "ingredients", List.of(Map.of("foodId", foodId, "grams", 200.0))
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apple Salad"))
                .andExpect(jsonPath("$.numberOfPortions").value(2))
                .andExpect(jsonPath("$.ingredients[0].foodId").value(foodId))
                .andExpect(jsonPath("$.ingredients[0].grams").value(200.0))
                .andExpect(jsonPath("$.total.kcal").value(104.0))
                .andExpect(jsonPath("$.total.proteinG").value(0.6))
                .andExpect(jsonPath("$.total.carbsG").value(28.0))
                .andExpect(jsonPath("$.total.fatG").value(0.4))
                .andExpect(jsonPath("$.perPortion.kcal").value(52.0))
                .andExpect(jsonPath("$.perPortion.proteinG").value(0.3))
                .andExpect(jsonPath("$.perPortion.carbsG").value(14.0))
                .andExpect(jsonPath("$.perPortion.fatG").value(0.2))
                .andExpect(jsonPath("$.deleted").value(false))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        assert json.get("id").isIntegralNumber();
    }

    // ── GET /recipes/{id} ────────────────────────────────────────────────────

    @Test
    void getRecipe_withoutToken_returns401() throws Exception {
        Long recipeId = createRecipe(2, foodId, 200.0);

        mockMvc.perform(get("/recipes/" + recipeId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getRecipe_returnsRecipeWithIngredientsAndNutrition() throws Exception {
        // 200g of Apple (52 kcal, 0.3g protein, 14g carbs, 0.2g fat per 100g), split into 2 portions.
        // Total: kcal=104, protein=0.6, carbs=28, fat=0.4 — Per portion: kcal=52, protein=0.3, carbs=14, fat=0.2
        Long recipeId = createRecipe(2, foodId, 200.0);

        mockMvc.perform(get("/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(recipeId))
                .andExpect(jsonPath("$.name").value("Apple Salad"))
                .andExpect(jsonPath("$.numberOfPortions").value(2))
                .andExpect(jsonPath("$.ingredients[0].foodId").value(foodId))
                .andExpect(jsonPath("$.ingredients[0].grams").value(200.0))
                .andExpect(jsonPath("$.total.kcal").value(104.0))
                .andExpect(jsonPath("$.perPortion.kcal").value(52.0))
                .andExpect(jsonPath("$.perPortion.proteinG").value(0.3))
                .andExpect(jsonPath("$.perPortion.carbsG").value(14.0))
                .andExpect(jsonPath("$.perPortion.fatG").value(0.2));
    }

    @Test
    void getRecipe_notFound_returns404() throws Exception {
        mockMvc.perform(get("/recipes/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ── PUT /recipes/{id} ────────────────────────────────────────────────────

    @Test
    void updateRecipe_replacesIngredientsAndRecomputesTotals() throws Exception {
        // Original: 200g Apple, 2 portions → total kcal=104, per portion kcal=52
        Long recipeId = createRecipe(2, foodId, 200.0);

        // Updated: 400g Apple, 4 portions → total kcal=208, per portion kcal=52 (unchanged)
        mockMvc.perform(put("/recipes/" + recipeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Apple Salad v2",
                                "numberOfPortions", 4,
                                "ingredients", List.of(Map.of("foodId", foodId, "grams", 400.0))
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple Salad v2"))
                .andExpect(jsonPath("$.numberOfPortions").value(4))
                .andExpect(jsonPath("$.ingredients.length()").value(1))
                .andExpect(jsonPath("$.ingredients[0].grams").value(400.0))
                .andExpect(jsonPath("$.total.kcal").value(208.0))
                .andExpect(jsonPath("$.total.proteinG").value(1.2))
                .andExpect(jsonPath("$.total.carbsG").value(56.0))
                .andExpect(jsonPath("$.total.fatG").value(0.8))
                .andExpect(jsonPath("$.perPortion.kcal").value(52.0))
                .andExpect(jsonPath("$.perPortion.proteinG").value(0.3))
                .andExpect(jsonPath("$.perPortion.carbsG").value(14.0))
                .andExpect(jsonPath("$.perPortion.fatG").value(0.2));

        // GET reflects the update
        mockMvc.perform(get("/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ingredients.length()").value(1))
                .andExpect(jsonPath("$.total.kcal").value(208.0));
    }

    @Test
    void updateRecipe_notFound_returns404() throws Exception {
        mockMvc.perform(put("/recipes/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Apple Salad v2",
                                "numberOfPortions", 4,
                                "ingredients", List.of(Map.of("foodId", foodId, "grams", 400.0))
                        ))))
                .andExpect(status().isNotFound());
    }

    // ── DELETE /recipes/{id} ─────────────────────────────────────────────────

    @Test
    void deleteRecipe_softDeletesAndGetReturns404() throws Exception {
        Long recipeId = createRecipe(2, foodId, 200.0);

        mockMvc.perform(delete("/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/recipes/" + recipeId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRecipe_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/recipes/999999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    // ── GET /recipes ─────────────────────────────────────────────────────────

    @Test
    void listRecipes_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/recipes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listRecipes_excludesDeletedRecipes() throws Exception {
        Long keptId = createRecipe(2, foodId, 200.0);
        Long deletedId = createRecipe(2, foodId, 200.0);

        mockMvc.perform(delete("/recipes/" + deletedId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/recipes")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + keptId + ")]").exists())
                .andExpect(jsonPath("$[?(@.id == " + deletedId + ")]").doesNotExist());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Long createRecipe(int numberOfPortions, Long ingredientFoodId, double grams) throws Exception {
        String response = mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", "Apple Salad",
                                "numberOfPortions", numberOfPortions,
                                "ingredients", List.of(Map.of("foodId", ingredientFoodId, "grams", grams))
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createFood(String name, double kcal, double proteinG, double carbsG, double fatG) throws Exception {
        String response = mockMvc.perform(post("/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "name", name + "-" + UUID.randomUUID(),
                                "energyKcal", kcal,
                                "proteinG", proteinG,
                                "carbsG", carbsG,
                                "fatG", fatG
                        ))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
