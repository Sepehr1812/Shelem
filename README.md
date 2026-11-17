# Shelem! (شلم!) — Android Scoresheet App

An offline-first, bilingual (Persian / English) Android scorekeeping and match analytics application for the classic 4-player Persian trick-taking card game **Shelem**, built with Kotlin, Jetpack Compose, Material Design 3, and Room.

---

## Key Features

- **3-Step Game Setup & Interactive Table Seating**
  - Configure team names and all 4 players (`Team 1` vs. `Team 2`).
  - Visual, interactive card-table layout with **counter-clockwise (پادساعتگرد) dealer rotation** and one-tap opponent seat swapping.
  - Customizable target score (e.g., `1165`, `1200`), optional **1000+ Point Rule** (`winHandOnlyOver1000`), and optional **Ace in Ground** (`trackAceInGround`) tracking.
- **Two-Phase Round Workflow**
  - **Bid Declaration (`AddRoundBottomSheet`)**: Select the Declarer/Hakem, auto-rotated Dealer, bid amount (`100`–`165`), and *Naras* toggle.
  - **Score Entry (`DeclareScoreBottomSheet`)**: Slider and quick-preset chips for raw points collected (`0`–`165`) with real-time score preview, Rule 1000+ restriction indicators, and optional Ace-in-Ground toggle.
- **Rich Match Analytics & Custom Canvas Charts**
  - Interactive **Score Progression Line Chart**, **Round-by-Round Bar Chart**, **Outcomes & Win-Share Donut Chart**, **5-Axis Team Comparison Radar Chart**, and **Bid Distribution Histogram** drawn directly with Jetpack Compose `Canvas`.
  - **Player Performance Leaderboard** (MVP Hakem badge, success rate, average bid, max bid made, net Hakem score) and **Match Records & Highlights** (biggest penalty, most precise/clutch bid, largest cumulative lead, luckiest dealer, Rule 1000+ blocks, and Ace-in-Ground statistics).
- **CSV Export & Sharing (`CsvExporter`)**
  - Exports full match metadata and a round-by-round breakdown table to `.csv` with a **UTF-8 BOM** (`\uFEFF`) for clean rendering of Persian and English text in Excel and Google Sheets.
  - Supports saving via Storage Access Framework (`ActivityResultContracts.CreateDocument`) or sharing directly through `FileProvider`.
- **Bilingual Localization & Adaptive Theming**
  - Defaults to **Persian (`fa`)** with RTL layout, `Vazir` font (`vazir_regular.ttf`), and automatic Persian/English numeral formatting (`PersianNumberHelper`).
  - Supports runtime Persian/English language switching on **Android 13+ (API 33+)** via `LocaleManager` and `locales_config.xml`.
  - Custom **Felt Green & Gold** Material 3 Dark and Light themes, toggled from the Navigation Drawer alongside repository and bug-report email actions.

---

## Tech Stack & Dependencies

| Category | Technology / Library | Version / Notes |
| --- | --- | --- |
| **Language** | Kotlin | `2.2.10` |
| **Build System** | Gradle (Kotlin DSL) + Version Catalog | AGP `9.1.1`, KSP `2.3.5` |
| **SDK Targets** | `minSdk = 24`, `compileSdk = 37`, `targetSdk = 37` | Java `11` bytecode |
| **UI Framework** | Jetpack Compose + Material Design 3 | Compose BOM `2026.09.00` |
| **Navigation** | Navigation Compose | `2.10.2` (`HOME`, `SETUP`, `BOARD`) |
| **Architecture** | MVVM + Repository Pattern | `AndroidViewModel`, `StateFlow`, `collectAsStateWithLifecycle` |
| **Local Persistence** | Room (`room-runtime`, `room-ktx`, `room-compiler`) | `2.8.5` (SQLite, DB version `5`) |
| **Async / Concurrency** | Kotlin Coroutines & Flow | `1.11.0` |
| **Graphics & Charts** | Jetpack Compose `Canvas` | Custom line, bar, donut, and radar charts |
| **Testing** | JUnit 4, Robolectric, Roborazzi, Compose UI Test | Robolectric `4.17`, Roborazzi `1.78.0` |

*Dependency versions are centrally managed in `gradle/libs.versions.toml`.*

---

## Project Structure

```text
.
├── build.gradle.kts                     # Top-level Gradle plugin declarations
├── settings.gradle.kts                  # Repository config & rootProject.name ("Shelem")
├── metadata.json                        # AI Studio platform metadata
├── gradle/
│   └── libs.versions.toml               # Centralized version catalog
└── app/
    ├── build.gradle.kts                 # App module config, signingConfigs, dependencies
    ├── proguard-rules.pro               # ProGuard/R8 rules
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml      # App entry, RTL & localeConfig, FileProvider
        │   ├── java/com/shelem/scoresheet/
        │   │   ├── MainActivity.kt      # Edge-to-edge host, locale context, ShelemTheme
        │   │   ├── data/
        │   │   │   ├── AppDatabase.kt       # Room database singleton (shelem_database)
        │   │   │   ├── GameEntity.kt        # Game table, rules & counter-clockwise seating
        │   │   │   ├── RoundEntity.kt       # Round table (FK cascade to GameEntity)
        │   │   │   ├── GameWithRounds.kt    # Room @Relation model & team score totals
        │   │   │   ├── ShelemDao.kt         # Reactive Flow queries & CRUD operations
        │   │   │   └── ShelemRepository.kt  # Data operations & auto-finish game evaluation
        │   │   ├── domain/
        │   │   │   ├── RoundResult.kt       # Sealed RoundResult, Team enum, CalculatedRoundScores
        │   │   │   ├── ScoringEngine.kt     # Pure Shelem scoring & Rule 1000+ logic
        │   │   │   └── GameStatisticsCalculator.kt # Aggregated KPIs, player stats & records
        │   │   ├── ui/
        │   │   │   ├── components/
        │   │   │   │   ├── GameCard.kt                  # Home screen match summary card
        │   │   │   │   ├── RoundCard.kt                 # Round card with swipe/delete & status
        │   │   │   │   ├── ScoreProgressionLineChart.kt # Interactive cumulative score chart
        │   │   │   │   ├── StatisticsCharts.kt          # Bar, donut, radar & bid charts
        │   │   │   │   └── PlayerAndRecordStatsCards.kt # Player leaderboard & match records
        │   │   │   ├── navigation/
        │   │   │   │   └── NavGraph.kt                  # Compose NavHost (home, setup, board/{gameId})
        │   │   │   ├── screens/
        │   │   │   │   ├── HomeScreen.kt                # Match list, ongoing/history, drawer
        │   │   │   │   ├── GameSetupScreen.kt           # 3-step setup & table seating wizard
        │   │   │   │   ├── GameBoardScreen.kt           # Live scoreboard, rounds & actions
        │   │   │   │   ├── AddRoundBottomSheet.kt       # Bid & Hakem/Dealer declaration sheet
        │   │   │   │   ├── DeclareScoreBottomSheet.kt   # Raw points & live score preview sheet
        │   │   │   │   ├── GameStatisticsBottomSheet.kt # Tabbed analytics & charts sheet
        │   │   │   │   └── ExportCsvDialog.kt           # CSV save & share bottom sheet
        │   │   │   ├── theme/
        │   │   │   │   ├── Color.kt         # Felt green, gold, emerald, red & surface palette
        │   │   │   │   ├── Theme.kt         # Light/Dark ColorSchemes & RTL/LTR direction
        │   │   │   │   └── Type.kt          # Material 3 Typography backed by VazirFontFamily
        │   │   │   └── viewmodel/
        │   │   │       └── ShelemViewModel.kt # UI state flows & ViewModel actions
        │   │   └── util/
        │   │       ├── CsvExporter.kt       # UTF-8 BOM CSV generator & FileProvider share
        │   │       ├── LocaleHelper.kt      # Default Persian locale & API 33+ switcher
        │   │       └── PersianNumberHelper.kt # Persian/English digit conversion & formatting
        │   └── res/
        │       ├── font/vazir_regular.ttf   # Bundled Vazir font
        │       ├── values/strings.xml       # Default (Persian) strings
        │       ├── values-fa/strings.xml    # Persian string resources
        │       ├── values-en/strings.xml    # English string resources
        │       ├── xml/locales_config.xml   # Per-app language config (fa, en)
        │       ├── xml/file_paths.xml       # FileProvider cache export path
        │       └── mipmap-* / drawable/     # Adaptive launcher icon & vector icons
        ├── test/
        │   ├── java/com/shelem/scoresheet/
        │   │   ├── ExampleUnitTest.kt              # Scoring, dealer rotation & stats unit tests
        │   │   ├── ExampleRobolectricTest.kt       # Bilingual terminology & bug-report intent tests
        │   │   ├── LocaleBehaviorRobolectricTest.kt # API < 33 vs API 33+ locale behavior tests
        │   │   └── GreetingScreenshotTest.kt       # Roborazzi Compose screenshot test
        │   └── screenshots/greeting.png            # Reference Roborazzi screenshot
        └── androidTest/
            └── java/com/shelem/scoresheet/
                └── ExampleInstrumentedTest.kt
```

---

## Architecture & Data Flow

The application follows a clean **MVVM + Repository** architecture with unidirectional data flow:

1. **Data Layer (`com.shelem.scoresheet.data`)**
   - `AppDatabase` manages the Room SQLite database (`shelem_database`, schema version `5`) containing `games` (`GameEntity`) and `rounds` (`RoundEntity`, linked via foreign key with `CASCADE` delete).
   - `ShelemDao` exposes reactive `Flow` queries (`getOngoingGamesWithRounds`, `getFinishedGamesWithRounds`, `getGameById`, `getRoundsForGame`).
   - `ShelemRepository` coordinates scoring calculations before persisting rounds and automatically marks a game finished when the target score or target point lead is reached (and re-opens it if a round is deleted).
2. **Domain Layer (`com.shelem.scoresheet.domain`)**
   - `ScoringEngine` is a stateless object encapsulating all Shelem scoring rules.
   - `GameStatisticsCalculator` computes `ComprehensiveGameStats` from completed rounds (win streaks, bid brackets, per-player Hakem metrics, 5-axis radar values, match records, and Ace-in-Ground statistics).
3. **UI Layer (`com.shelem.scoresheet.ui`)**
   - `ShelemViewModel` exposes `StateFlow` streams (`ongoingGamesWithRounds`, `finishedGamesWithRounds`, `activeGame`, `activeRounds`, `darkThemeEnabled`).
   - `ShelemNavGraph` manages routing between `home`, `setup`, and `board/{gameId}` destinations.

---

## Shelem Scoring & Game Rules

Each standard hand contains **165 total card points**. Given the Declarer (*Hakem*) team's bid (`bidAmount`) and collected raw points (`hakemCollectedPoints`):

| Outcome | Condition | Declarer Team Score | Opponent Team Score |
| --- | --- | --- | --- |
| **Shelem (شلم)** | `hakemCollectedPoints == 165` | `+330` | `0` |
| **Made (پُر)** | `hakemCollectedPoints >= bidAmount` (and `< 165`) | `+hakemCollectedPoints` | `165 - hakemCollectedPoints` |
| **Set (خالی)** | `85 <= hakemCollectedPoints < bidAmount` | `-bidAmount` | `165 - hakemCollectedPoints` |
| **Double Set (دوبل منفی)** | `hakemCollectedPoints < 85` | `-2 * bidAmount` | `165 - hakemCollectedPoints` |

- **1000+ Point Rule (`winHandOnlyOver1000`)**: When enabled (default), if a team's prior cumulative score is `>= 1000`, it receives `0` points (instead of positive opponent points) unless it wins that hand (accomplishes its own bid/Shelem or sets the opponent).
- **Match Win Condition**: A match automatically finishes when either team reaches `targetScore` **or** establishes a lead difference `abs(team1Total - team2Total) >= targetScore`. Matches can also be manually finished or resumed from the Game Board.
- **Dealer Rotation**: Counter-clockwise order around the table: Seat `0` (Bottom / You) → Seat `1` (Right Opponent) → Seat `2` (Top / Teammate) → Seat `3` (Left Opponent).

---

## Localization & Number Formatting

- **Default Locale**: The app initializes in **Persian (`fa`)** across all Android versions via `LocaleHelper.attachBaseContext` and `ensureDefaultPersianLocale`.
- **Runtime Switching**: On Android 13+ (API 33+), users can switch between Persian (`fa`, RTL) and English (`en`, LTR) from the Home Navigation Drawer via `LocaleManager`. Below API 33, the app locks to Persian and hides the drawer language toggle.
- **Numerals**: `PersianNumberHelper` formats scores and numbers using Persian digits (`۰`–`۹`) with proper RTL negative sign handling when Persian is active, and standard ASCII digits when English is active.

---

## Building & Running Tests

> Note: Run Gradle commands directly with `gradle` (or `./gradlew` in standard local Android Studio environments where the Gradle wrapper is installed).

### Build Debug APK
```bash
gradle :app:assembleDebug
```

### Run Unit, Robolectric & Roborazzi Tests
```bash
gradle :app:testDebugUnitTest
```
