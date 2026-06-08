package com.calorietracker.diary;

import com.calorietracker.AbstractIntegrationTest;
import com.calorietracker.recipe.Recipe;
import com.calorietracker.recipe.RecipeIngredient;
import com.calorietracker.recipe.RecipeIngredientRepository;
import com.calorietracker.recipe.RecipeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class DiaryControllerTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired RecipeRepository recipeRepository;
    @Autowired RecipeIngredientRepository recipeIngredientRepository;

    private String token;
    private Long userId;
    private Long foodId;

    @BeforeEach
    void setup() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        String regResponse = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "password", "s3cr3t!!"
                        ))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode regJson = objectMapper.readTree(regResponse);
        token = regJson.get("accessToken").asText();
        userId = regJson.get("user").get("id").asLong();
        foodId = createFood("Apple", 52.0, 0.3, 14.0, 0.2);
    }

    // ── POST /diary ──────────────────────────────────────────────────────────

    @Test
    void createEntry_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "entryDate", "2026-06-08",
                                "mealType", "BREAKFAST",
                                "sourceType", "FOOD",
                                "foodId", foodId,
                                "quantity", 150.0
                        ))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createEntry_foodSource_snapshotsNutrition() throws Exception {
        // 150g of Apple (52 kcal, 0.3g protein, 14g carbs, 0.2g fat per 100g)
        // Expected: kcal=78.0, proteinG=0.45, carbsG=21.0, fatG=0.3
        String response = mockMvc.perform(post("/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "entryDate", "2026-06-08",
                                "mealType", "BREAKFAST",
                                "sourceType", "FOOD",
                                "foodId", foodId,
                                "quantity", 150.0
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.mealType").value("BREAKFAST"))
                .andExpect(jsonPath("$.sourceType").value("FOOD"))
                .andExpect(jsonPath("$.quantity").value(150.0))
                .andExpect(jsonPath("$.kcal").value(78.0))
                .andExpect(jsonPath("$.proteinG").value(0.45))
                .andExpect(jsonPath("$.carbsG").value(21.0))
                .andExpect(jsonPath("$.fatG").value(0.3))
                .andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        // id must be a valid UUID
        UUID.fromString(json.get("id").asText());
    }

    @Test
    void createEntry_recipePortionSource_snapshotsPerPortionNutrition() throws Exception {
        // Recipe: 2 portions. Ingredient: 200g of Apple (52 kcal, 0.3g protein, 14g carbs, 0.2g fat per 100g).
        // Total recipe: kcal=104, protein=0.6, carbs=28, fat=0.4
        // Per portion: kcal=52, protein=0.3, carbs=14, fat=0.2
        // Logging 1 portion → expects those per-portion values
        Long recipeId = createRecipe(2, foodId, 200.0);

        mockMvc.perform(post("/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "entryDate", "2026-06-08",
                                "mealType", "LUNCH",
                                "sourceType", "RECIPE_PORTION",
                                "recipeId", recipeId,
                                "quantity", 1.0
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sourceType").value("RECIPE_PORTION"))
                .andExpect(jsonPath("$.kcal").value(52.0))
                .andExpect(jsonPath("$.proteinG").value(0.3))
                .andExpect(jsonPath("$.carbsG").value(14.0))
                .andExpect(jsonPath("$.fatG").value(0.2));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Long createRecipe(int portions, Long ingredientFoodId, double ingredientGrams) {
        Recipe recipe = new Recipe();
        recipe.setUserId(userId);
        recipe.setName("TestRecipe-" + UUID.randomUUID());
        recipe.setNumberOfPortions(portions);
        recipe.setUpdatedAt(OffsetDateTime.now());
        recipe.setDeleted(false);
        Long recipeId = recipeRepository.save(recipe).getId();

        RecipeIngredient ingredient = new RecipeIngredient();
        ingredient.setRecipeId(recipeId);
        ingredient.setFoodId(ingredientFoodId);
        ingredient.setGrams(ingredientGrams);
        recipeIngredientRepository.save(ingredient);

        return recipeId;
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
