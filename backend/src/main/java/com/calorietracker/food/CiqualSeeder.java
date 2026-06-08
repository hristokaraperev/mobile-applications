package com.calorietracker.food;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the ANSES CIQUAL 2020 dataset into the {@code foods} table on first boot.
 * Skips seeding when CIQUAL rows are already present so restarts are safe.
 */
@Component
public class CiqualSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CiqualSeeder.class);
    private static final String CSV_PATH = "data/ciqual-2020.csv";
    private static final int BATCH_SIZE = 500;

    private final FoodRepository foodRepository;

    public CiqualSeeder(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (foodRepository.existsBySource(FoodSource.CIQUAL)) {
            log.info("CIQUAL data already loaded – skipping seed.");
            return;
        }

        log.info("Seeding CIQUAL 2020 dataset…");
        ClassPathResource resource = new ClassPathResource(CSV_PATH);

        List<Food> batch = new ArrayList<>(BATCH_SIZE);
        int total = 0;

        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(reader);

            OffsetDateTime now = OffsetDateTime.now();

            for (CSVRecord record : records) {
                Food food = new Food();
                food.setName(record.get("name"));
                food.setType(FoodType.RAW);
                food.setSource(FoodSource.CIQUAL);
                food.setEnergyKcal(parseDouble(record.get("energy_kcal")));
                food.setProteinG(parseDouble(record.get("protein_g")));
                food.setCarbsG(parseDouble(record.get("carbs_g")));
                food.setFatG(parseDouble(record.get("fat_g")));
                food.setSugarsG(parseDouble(record.get("sugars_g")));
                food.setSatFatG(parseDouble(record.get("sat_fat_g")));
                food.setFiberG(parseDouble(record.get("fiber_g")));
                food.setSaltG(parseDouble(record.get("salt_g")));
                food.setUpdatedAt(now);

                batch.add(food);
                if (batch.size() == BATCH_SIZE) {
                    foodRepository.saveAll(batch);
                    total += batch.size();
                    batch.clear();
                }
            }
        }

        if (!batch.isEmpty()) {
            foodRepository.saveAll(batch);
            total += batch.size();
        }

        log.info("CIQUAL seed complete – {} foods inserted.", total);
    }

    /**
     * Returns {@code null} for empty or non-numeric values (e.g. "traces").
     */
    private static Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
