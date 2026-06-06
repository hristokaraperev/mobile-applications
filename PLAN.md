# Plan: Calorie Tracker — Android app + Spring Boot API

## Context

This is the course project from [task.md](task.md): a **mobile app + a separate hosted API + a database**, with
registration/login and CRUD on a real resource. The chosen theme is a **personal calorie tracker**.

Goals the app must serve (from the user's request):
- Log foods into **today's meals** — breakfast / lunch / dinner / snack.
- Add packaged foods by **scanning a barcode with the camera**.
- Add **raw/whole foods** as well as packaged ones.
- Create **recipes**, split them into **portions**, and log a portion into a meal in one tap.
- Calorie data must come from **European sources only — no USDA dependency**.

Decisions confirmed with the user:
- **Data sources:** Open Food Facts (packaged/barcode) + ANSES CIQUAL (raw foods) + **user-contributed local labels**.
- **Auth:** JWT email + password (Spring Security + BCrypt).
- **Offline:** **offline-first** — local Room cache mirrors the diary and **syncs** to the API.

The repository is currently empty (only `task.md`, `task.pdf`, `README.md`, `.gitignore`) — this is a from-scratch build.
Tech stack is fixed by [README.md](README.md): **Kotlin/Android front-end, Java/Spring Boot API, PostgreSQL**, containerized + hosted.

## Requirement coverage (task.md)

| Task requirement | Covered by |
|---|---|
| Min. 3 screens | Auth, Diary/Home, Food search, Barcode scanner, Food detail, Recipe list, Recipe editor, Profile (8 screens) |
| Separated API (app never touches DB) | App → Spring Boot REST API → Postgres. Even Open Food Facts is proxied through the API |
| Database (not files/in-memory) | PostgreSQL via Spring Data JPA + Flyway migrations |
| Registration + Login | JWT email/password, Spring Security + BCrypt |
| CRUD on a main resource | Full CRUD on **Recipes** and on **Diary entries** |
| Hosted API during defense | Docker image on Render (managed Postgres), HTTPS via platform TLS |

---

## Data sourcing strategy (European, no USDA)

All third-party calls go **through the backend**, never from the phone — this honours the "separated API" rule, keeps a
custom `User-Agent`, lets us cache, and centralizes attribution.

1. **Open Food Facts** — packaged foods by barcode.
   - Endpoint: `GET https://world.openfoodfacts.org/api/v2/product/{ean}?fields=code,product_name,brands,nutriments,serving_size,quantity`
   - No API key. **Required:** custom `User-Agent` (e.g. `CalorieTracker/1.0 (contact email)`). Rate limit ~300 req/min on the product endpoint → our backend **caches every fetched product into Postgres** so each barcode hits OFF at most once.
   - Licence: **ODbL** → add an attribution line in the app's About/Settings screen.
2. **ANSES CIQUAL** — raw/whole foods (~3,484 items, per-100 g macros).
   - Download the CIQUAL 2020 table (CSV/Excel) from ANSES / data.gouv.fr once, transform to a seed CSV committed to the repo, and **load it into Postgres at startup** via a Flyway migration or a `CommandLineRunner` seeder. Attribution: "ANSES-CIQUAL".
3. **User-contributed local labels** — when a barcode/search misses (e.g. a Bulgarian product not in OFF), the user fills a short form (name, per-100 g nutrition, optional barcode) and we `POST /foods` with `source = USER`. These are reusable in later searches.

A single `foods` table unifies all three via a `source` enum and a `type` (PACKAGED / RAW) flag.

---

## Backend — Java / Spring Boot

**Modules/deps:** Spring Web, Spring Security + `jjwt`, Spring Data JPA, PostgreSQL driver, Flyway, Bean Validation,
`springdoc-openapi` (Swagger UI for the defense), `RestClient`/`WebClient` (OFF calls), Lombok (optional).

**Package layout** (`com.calorietracker`): `auth`, `user`, `food`, `recipe`, `diary`, `integration.off` (Open Food Facts client),
`config` (security, CORS, OpenAPI), `common` (error handling, DTOs).

**Entities / tables** (Flyway-managed):
- `users` — id, email (unique), password_hash, display_name, daily_kcal_goal, created_at.
- `foods` — id, name, brand, barcode (nullable, indexed), type (PACKAGED/RAW), source (OFF/CIQUAL/USER),
  per-100g: energy_kcal, protein_g, carbs_g, sugars_g, fat_g, sat_fat_g, fiber_g, salt_g; serving_size_g (nullable), owner_user_id (nullable for USER foods), updated_at.
- `recipes` — id, user_id, name, number_of_portions, total_cooked_weight_g (nullable), updated_at, deleted (soft-delete for sync).
- `recipe_ingredients` — id, recipe_id, food_id, grams.
- `diary_entries` — id (UUID, client-generatable), user_id, entry_date, meal_type (BREAKFAST/LUNCH/DINNER/SNACK),
  source_type (FOOD/RECIPE_PORTION), food_id (nullable), recipe_id (nullable), quantity (grams for food, portions for recipe),
  snapshot nutrition (kcal/protein/carbs/fat), updated_at, deleted.

**Recipe → portion math:** recipe total nutrition = Σ(ingredient per-100 g × grams / 100). One portion =
total ÷ `number_of_portions`. Logging a recipe portion writes a `diary_entry` with `source_type = RECIPE_PORTION` and a
`quantity` of portions (default 1, editable e.g. 0.5 / 2); nutrition is snapshotted so later recipe edits don't rewrite history.

**REST endpoints:**
- `POST /auth/register`, `POST /auth/login` → `{ accessToken, user }`.
- `GET /foods/search?q=` , `GET /foods/barcode/{ean}` (cache → OFF → 404), `GET /foods/{id}`, `POST /foods` (user label).
- `GET/POST/PUT/DELETE /recipes`, `GET /recipes/{id}` — full CRUD, returns computed totals + per-portion values.
- `GET /diary?date=` , `GET /diary/summary?date=` (meal + daily totals), `POST /diary`, `PUT /diary/{id}`, `DELETE /diary/{id}`.
- `GET /diary/changes?since=<timestamp>` + `POST /diary/sync` (batch upsert) — drives offline sync.

Security: all `/auth/**`, Swagger, and the OFF proxy are open; everything else requires a valid JWT. CORS enabled.
Standard JSON error envelope via `@RestControllerAdvice`.

---

## Android — Kotlin

**Stack:** Jetpack Compose + Navigation Compose, MVVM, Hilt (DI), Retrofit + OkHttp + kotlinx.serialization,
**Room** (offline cache), **DataStore** (JWT token), **CameraX + ML Kit Barcode Scanning** (`FORMAT_EAN_13`,
`STRATEGY_KEEP_ONLY_LATEST`), WorkManager (sync), Coroutines/Flow.

**Screens:**
1. **Auth** — login / register, stores JWT in DataStore.
2. **Diary / Home** — date selector; four meal sections with entries and per-meal + daily kcal totals vs goal; tap "+" on a meal to add.
3. **Food search** — text search (server + local cache), result list, prominent **Scan barcode** button, "Add custom food" fallback.
4. **Barcode scanner** — CameraX preview + ML Kit; on detect → `GET /foods/barcode/{ean}` → food detail (or custom-food form on miss).
5. **Food detail / add-to-meal** — quantity in grams (or servings), meal-type picker, live nutrition preview, save → diary.
6. **Recipes list** — user recipes with per-portion kcal; create / edit / delete.
7. **Recipe editor** — name, ingredients (search & add foods with grams), number of portions; shows totals + per-portion; "Log a portion" → meal picker.
8. **Profile / settings** — daily kcal goal, data-source attribution (OFF ODbL + ANSES-CIQUAL), logout.

**Offline-first sync model** (the most complex part — build last, can be staged):
- Room mirrors `diary_entries`, `recipes`, and cached `foods`. The UI reads from Room (single source of truth) so it works offline.
- Diary/recipe rows use **client-generated UUIDs** + a `syncState` (SYNCED / PENDING_CREATE / PENDING_UPDATE / PENDING_DELETE) and `updatedAt`.
- A WorkManager `SyncWorker` (on connectivity / on change): push pending rows via `POST /diary/sync`, then pull `GET /diary/changes?since=lastSync` and upsert. **Last-write-wins by `updatedAt`**; deletes are soft-deletes.
- Foods are cached read-only after lookup so logged items render offline.

---

## Build order (incremental, each step demoable)

1. **Backend skeleton + DB:** Spring Boot, Postgres via Docker Compose, Flyway baseline, `users`/`foods`/`recipes`/`diary` tables, Swagger.
2. **Auth:** register/login, JWT filter, BCrypt; secure endpoints.
3. **Foods:** CIQUAL seed loader, search, user-food create.
4. **Open Food Facts proxy:** barcode endpoint with Postgres caching + custom User-Agent.
5. **Diary CRUD** + daily summary.
6. **Recipes CRUD** + portion math + log-a-portion.
7. **Android shell:** Compose nav, Hilt, Retrofit, DataStore, auth screens.
8. **Diary + food search + food detail** (online).
9. **Barcode scanner** (CameraX + ML Kit) → barcode lookup flow.
10. **Recipes** UI (list, editor, log portion).
11. **Room offline cache + WorkManager sync.**
12. **Dockerize + deploy** to Render (managed Postgres), wire app `baseUrl` to the hosted HTTPS URL.

---

## Deployment

- Backend `Dockerfile` (multi-stage: Maven build → slim JRE). Default target **Render** free tier with a managed Postgres
  add-on; **Railway / Fly.io** are equivalent fallbacks. HTTPS/TLS provided by the platform (covers the README nice-to-have).
- Config (DB URL, JWT secret, OFF User-Agent) via environment variables.
- App points at the deployed HTTPS base URL; Android network-security config allows it.

## Verification

- **Backend:** Swagger UI / `curl` against the hosted URL — register → login → search a CIQUAL food → look up a real
  EAN barcode (returns OFF data, second call served from cache) → create a recipe and confirm per-portion kcal → CRUD a
  diary entry → check `GET /diary/summary` daily totals. Integration tests with Testcontainers-Postgres for auth + diary CRUD.
- **App:** run on a device/emulator — register/login; scan a real product barcode and log it to lunch; add a raw food (e.g.
  CIQUAL apple) by grams; build a recipe, split into portions, log one portion to dinner; verify the diary day total updates;
  enable airplane mode, add an entry, re-enable network, confirm it syncs (visible via Swagger on the server).
- **Requirements check:** 8 screens (≥3 ✓), app↔API↔DB separation ✓, Postgres ✓, JWT auth ✓, CRUD on recipes & diary ✓,
  hosted HTTPS API reachable during the defense ✓.
