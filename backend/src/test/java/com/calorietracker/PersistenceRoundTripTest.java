package com.calorietracker;

import com.calorietracker.diary.DiaryEntry;
import com.calorietracker.diary.DiaryRepository;
import com.calorietracker.diary.DiarySourceType;
import com.calorietracker.diary.MealType;
import com.calorietracker.user.User;
import com.calorietracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves the entities actually round-trip through the database: a database-generated
 * identity key for {@link User}, and a client-supplied UUID key with enum columns for
 * {@link DiaryEntry}.
 */
@Transactional
class PersistenceRoundTripTest extends AbstractIntegrationTest {

    @Autowired
    UserRepository users;

    @Autowired
    DiaryRepository diary;

    @Test
    void userGetsGeneratedIdentity() {
        User user = new User();
        user.setEmail("roundtrip@example.com");
        user.setPasswordHash("hash");
        user.setCreatedAt(OffsetDateTime.now());

        User saved = users.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(users.findByEmail("roundtrip@example.com")).isPresent();
    }

    @Test
    void diaryEntryPersistsWithClientUuidAndEnums() {
        User owner = new User();
        owner.setEmail("diary-owner@example.com");
        owner.setPasswordHash("hash");
        owner.setCreatedAt(OffsetDateTime.now());
        Long ownerId = users.save(owner).getId();

        UUID id = UUID.randomUUID();
        DiaryEntry entry = new DiaryEntry();
        entry.setId(id);
        entry.setUserId(ownerId);
        entry.setEntryDate(LocalDate.now());
        entry.setMealType(MealType.LUNCH);
        entry.setSourceType(DiarySourceType.FOOD);
        entry.setQuantity(150.0);
        entry.setKcal(240.0);
        entry.setUpdatedAt(OffsetDateTime.now());

        diary.save(entry);

        DiaryEntry found = diary.findById(id).orElseThrow();
        assertThat(found.getMealType()).isEqualTo(MealType.LUNCH);
        assertThat(found.getSourceType()).isEqualTo(DiarySourceType.FOOD);
    }
}
