# Android Scholastic Crossword Wordcraft Game

Wordcraft is a modern, responsive, client-side Android word puzzle game built with Kotlin, Jetpack Compose, and Material 3 design guidelines. The game features an education-themed aesthetic ("Classroom", "Library", etc.), interactive custom word wheels, crossword grid structures, persistence via a Room database, leaderboard ranks, sound effects, and haptic feedback.

---

## 🏗️ Architecture Overview

The codebase is built on the **Model-View-ViewModel (MVVM)** architectural pattern combined with structured repository and database-layer abstraction to maintain a high-quality, reactive, and robust workflow.

```
┌─────────────────────────────────────────────────────────┐
│                        UI LAYER                         │
│  (HomeScreen, GameplayScreen, DailyChallengeScreen, etc) │
└────────────┬────────────────────────────────▲───────────┘
             │                                │
             │ User Actions (Shuffles,        │ Observes UI /
             │ Swipes, Purchases)             │ State Updates
             ▼                                │
┌─────────────────────────────────────────────────────────┐
│                     VIEWMODEL LAYER                     │
│                     (GameViewModel)                     │
└────────────┬────────────────────────────────▲───────────┘
             │                                │
             │ Reads / Updates                │ Exposes Flow
             │ State                          │ Data
             ▼                                │
┌─────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                     │
│                    (GameRepository)                     │
└────────────┬────────────────────────────────▲───────────┘
             │                                │
             │ Database                       │ Reactive
             │ Queries                        │ Query Results
             ▼                                │
┌─────────────────────────────────────────────────────────┐
│                       DATA LAYER                        │
│             (Room Database, UserProgressDao)            │
└─────────────────────────────────────────────────────────┘
```

### 1. Data Layer (Model & Persistence)
*   **Room Database (`AppDatabase.kt`)**: Implements real-world structured SQLite persistence on-device.
*   **Entities (`GameModels.kt`)**:
    *   `UserStats`: Manages the local persistence of current level metrics, coins count, star count, player name, and user preferences (sound, haptic toggles).
    *   `LevelGameplayState`: Tracks crossword character grid completions (solved words, hints coordinates) on specific levels, ensuring state persistence across application launches.
    *   `LeaderboardEntry`: Controls scholastic entries (including AI bots) to keep the leaderboard dynamic and active.
    *   `DailyChallengeState`: Monitors completion data and awards for specific dates.
*   **Data Access Object (`UserProgressDao.kt`)**: Decouples active queries from thread-blocking bottlenecks using asynchronous Kotlin `Coroutines` and reactive `Flow` representations.

### 2. Repository Layer
*   **`GameRepository.kt`**: Coordinates data sources. Actively handles synchronization logic, triggers state validation, persists gameplay progression, and exposes reactive flows to the view-model cleanly.

### 3. ViewModel Layer
*   **`GameViewModel.kt`**: Holds UI-agnostic presentation logic. Exposes highly-granular StateFlows containing state definitions (such as current game screen, solved words, user status details, active letter wheels, toast alerts). Manages the mechanics for purchase validators, anagram shufflers, game timers, and level completions.

### 4. UI Layer
*   Written entirely in **Jetpack Compose** with Material 3 components, high-fidelity responsive layouts, custom interactive components, and dynamic typography.

---

## 🎡 Custom Circular Swipe Wheel (`GameLetterWheel`)

The custom swipe interface utilizes precise geometry and Gestures pointer-input APIs to handle touch events with responsive feedback.

### 1. Geometry Calculations
To place letters in a circular layout, trigonometry computes individual node locations dynamically inside a bounded circle container:

*   **Positions Determination**:
    $$\text{Angle Offset} = -\frac{\pi}{2} + \left(i \times \frac{2\pi}{N}\right)$$
    $$\text{Position}_x = \text{Center}_x + \text{Radius} \times \cos(\text{Angle Offset})$$
    $$\text{Position}_y = \text{Center}_y + \text{Radius} \times \sin(\text{Angle Offset})$$
    Where $N$ is the total count of letters placed symmetrically along the circle perimeter.

### 2. Pointer Gestures Processing
*   **`pointerInput` & `awaitPointerEventScope`**: Directly processes raw pointer sequences (`awaitFirstDown`, `awaitPointerEvent`) asynchronously.
*   **Collision Detection**: Whenever a user drags their pointer, distances between the current coordinate and all letter node coordinates are validated:
    $$\Delta_x = x_{\text{touch}} - x_{\text{node}}, \quad \Delta_y = y_{\text{touch}} - y_{\text{node}}$$
    $$\text{Distance}^2 = \Delta_x^2 + \Delta_y^2 < \text{Collision Radius}^2$$
    If a touch falls within the designated collision radius boundary (e.g., 32dp), that node index is added to the selection tracking chain.
*   **Retraction Feedback (Back-Swiping)**: Supports interactive word correction. If the pointer returns over the second-to-last node in the current chain, the final node is deleted from the active word path, and an updated state flow is published instantly.
*   **Canvas Connected Lines**: Draws connection strokes inside a responsive `Canvas` overlaying the circle bounds using high-contrast orange coordinates. Uses stroke caps with circular profiles and draws real-time guidelines up to the last active pointer touch offset.

---

## 📊 Level & Grid Structures

Levels are modeled via a static schema configuration that abstracts how rows, columns, grids, and words represent themselves globally.

### 1. Representation & Schema Configuration
```kotlin
data class WordPlacement(
    val word: String,
    val startRow: Int,
    val startCol: Int,
    val isHorizontal: Boolean
)

data class LevelSpec(
    val id: Int,
    val themeName: String,
    val colorStyle: String,
    val letters: String,
    val gridWords: List<String>,
    val placements: List<WordPlacement>,
    val bonusWords: List<String>
)
```

### 2. Grid Coordinate Mapping
Crossword puzzle layouts are dynamically compiled during runtime using custom geometry mapping functions inside `LevelSpec`:

*   **`computeGridCells()`**: Merges absolute overlapping crossword entries. Iterates over active word placements and populates 2D grids as map coordinate structures:
    $$\text{Coordinate Mapping} \rightarrow \text{CellsMap}[(\text{Row}, \text{Col})] = \text{Char}$$
*   This mapping evaluates whether grid elements are crossed correctly, and enables rendering letter hints securely over dynamic Cartesian grids without storing massive, redundant multidimensional arrays.
