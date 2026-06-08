package com.calorietracker.food;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OffClientImplTest {

    private MockRestServiceServer server;
    private OffClientImpl client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new OffClientImpl(builder);
    }

    @Test
    void fetchByBarcode_whenOffReturns404_returnsEmpty() {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v2/product/0000000000000")))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        Optional<Food> result = client.fetchByBarcode("0000000000000");

        assertThat(result).isEmpty();
    }

    @Test
    void fetchByBarcode_whenOffReturnsStatusZero_returnsEmpty() {
        String body = """
                {"status":0,"status_verbose":"product not found","product":null}
                """;
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v2/product/1111111111111")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        Optional<Food> result = client.fetchByBarcode("1111111111111");

        assertThat(result).isEmpty();
    }

    @Test
    void fetchByBarcode_whenOffReturnsProduct_mapsNutrition() {
        String body = """
                {
                  "status": 1,
                  "product": {
                    "product_name": "Nutella",
                    "brands": "Ferrero",
                    "code": "3017620422003",
                    "nutriments": {
                      "energy-kcal_100g": 539,
                      "proteins_100g": 6.3,
                      "carbohydrates_100g": 57.5,
                      "fat_100g": 30.9
                    }
                  }
                }
                """;
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/api/v2/product/3017620422003")))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));

        Optional<Food> result = client.fetchByBarcode("3017620422003");

        assertThat(result).isPresent();
        Food food = result.get();
        assertThat(food.getName()).isEqualTo("Nutella");
        assertThat(food.getBrand()).isEqualTo("Ferrero");
        assertThat(food.getBarcode()).isEqualTo("3017620422003");
        assertThat(food.getSource()).isEqualTo(FoodSource.OFF);
        assertThat(food.getType()).isEqualTo(FoodType.PACKAGED);
        assertThat(food.getEnergyKcal()).isEqualTo(539.0);
        assertThat(food.getProteinG()).isEqualTo(6.3);
    }
}
