package com.calorietracker.food;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Business logic for food search and creation.
 */
@Service
public class FoodService {

    private static final int SEARCH_LIMIT = 50;

    private final FoodRepository foodRepository;
    private final OffClient offClient;

    public FoodService(FoodRepository foodRepository, OffClient offClient) {
        this.foodRepository = foodRepository;
        this.offClient = offClient;
    }

    /**
     * Returns up to {@value #SEARCH_LIMIT} foods whose name contains {@code query}, ordered alphabetically.
     */
    public List<FoodResponse> search(String query) {
        return foodRepository
                .findByNameContainingIgnoreCaseOrderByNameAsc(query, PageRequest.of(0, SEARCH_LIMIT))
                .stream()
                .map(FoodResponse::from)
                .toList();
    }

    /** Returns a single food by its database ID, or 404 if not found. */
    public FoodResponse findById(Long id) {
        return foodRepository.findById(id)
                .map(FoodResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Returns food by barcode EAN. Checks Postgres cache first; falls back to Open Food Facts.
     * Persists the OFF result on a cache miss. Throws 404 if not found anywhere.
     */
    public FoodResponse lookupByBarcode(String ean) {
        return foodRepository.findByBarcode(ean)
                .or(() -> offClient.fetchByBarcode(ean).map(food -> {
                    food.setUpdatedAt(OffsetDateTime.now());
                    return foodRepository.save(food);
                }))
                .map(FoodResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a user-contributed food label owned by {@code ownerUserId}.
     */
    public FoodResponse create(CreateFoodRequest req, Long ownerUserId) {
        Food food = new Food();
        food.setName(req.name());
        food.setBrand(req.brand());
        food.setBarcode(req.barcode());
        food.setType(FoodType.PACKAGED);
        food.setSource(FoodSource.USER);
        food.setEnergyKcal(req.energyKcal());
        food.setProteinG(req.proteinG());
        food.setCarbsG(req.carbsG());
        food.setSugarsG(req.sugarsG());
        food.setFatG(req.fatG());
        food.setSatFatG(req.satFatG());
        food.setFiberG(req.fiberG());
        food.setSaltG(req.saltG());
        food.setServingSizeG(req.servingSizeG());
        food.setOwnerUserId(ownerUserId);
        food.setUpdatedAt(OffsetDateTime.now());

        return FoodResponse.from(foodRepository.save(food));
    }
}
