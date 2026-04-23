# EquimaxLab

> Kotlin Multiplatform poker study app — equity calculator, GTO-inspired trainer and hand replayer. Shared Kotlin core with Compose Multiplatform UI for Android, iOS and Web (Wasm).

---

##  Screenshots

| Calculator | Trainer | Replayer |
<img width="400"  alt="WhatsApp Image 2026-04-23 at 10 47 43 (1)" src="https://github.com/user-attachments/assets/e8399e07-81de-4c18-8421-cd7a06d6c2d2" />
<img width="400"  alt="WhatsApp Image 2026-04-23 at 10 47 43" src="https://github.com/user-attachments/assets/100926e9-eb97-4f34-9d23-25755bac5b61" />
<img width="400"  alt="WhatsApp Image 2026-04-23 at 10 47 43 (3)" src="https://github.com/user-attachments/assets/388004eb-9199-4e29-b022-22990a78aa72" />
<img width="400"  alt="WhatsApp Image 2026-04-23 at 10 47 43 (2)" src="https://github.com/user-attachments/assets/691b9620-0f78-4bbe-86a6-4b6ae65f95db" />


##  Features

### Equity Calculator
- Select your hole cards and up to 5 board cards via a visual card picker
- Optional opponent range input using standard poker notation (`QQ+`, `AKs`, `AQo`, etc.)
- Real-time equity calculation powered by a **custom Monte Carlo simulation engine** written in pure Kotlin (shared across all platforms)
- Displays win/tie/loss percentages with a color-coded equity bar
- Shows current hand strength (Royal Flush, Straight, Pair, etc.)

### GTO-Inspired Trainer
- Randomized scenarios with configurable filters:
  - **Position** — UTG, UTG+1, UTG+2, LJ, HJ, CO, BTN, SB, BB
  - **Street** — Preflop, Flop, Turn, River
  - **Preflop action** — SRP, 3-Bet, 4-Bet, Limp
  - **Stack depth** — Short (<20bb), Medium (20-40bb), Deep (40-100bb), Very Deep (>100bb)
- Full preflop action sequence with realistic context (raises, callers, position)
- Street-by-street action history with coherent hero actions on past streets
- Decision scoring system: **Best / Correct / Inexact / Bad / Blunder**
- Accuracy tracking with session statistics and streak counter
- Action buttons with concrete bb sizes (Fold / Call Xbb / Raise Xbb)
- Visual poker table with positions arranged in a circle (Compose Canvas)

### Hand Replayer
- Manual mode: enter players (up to 6), hole cards, and street-by-street actions
- Import mode: paste PokerStars / GGPoker hand history text for automatic parsing
- Full action input per player per street: Fold, Check, Call, Bet, Raise, All-in — with bet size in bb
- Save up to 5 hands with optional notes
- Street-by-street summary view with complete action log

---

##  Architecture

The project follows **Clean Architecture** with a strict separation between layers, designed to maximize code sharing across platforms via KMP.

```
composeApp/
└── src/
    ├── commonMain/
    │   ├── core/              # Pure Kotlin engine — no platform dependencies
    │   │   ├── Card.kt
    │   │   ├── Deck.kt
    │   │   ├── HandEvaluator.kt
    │   │   ├── MonteCarloSimulator.kt
    │   │   └── RangeParser.kt
    │   ├── domain/
    │   │   ├── model/         # Data models
    │   │   ├── repository/    # Repository interfaces
    │   │   └── usecase/       # Use cases
    │   ├── presentation/
    │   │   ├── calculator/    # CalculatorViewModel
    │   │   ├── trainer/       # TrainerViewModel + TrainerConfig
    │   │   └── replayer/      # ReplayerViewModel + ReplayerModels
    │   └── ui/
    │       ├── components/    # CardView, CardPickerDialog, PokerTable
    │       ├── navigation/    # BottomNav
    │       ├── screen/        # Calculator, Trainer, Replayer screens
    │       └── theme/         # Colors, Typography
    ├── androidMain/
    ├── iosMain/
    └── wasmJsMain/
```

### Core Engine

The `core` module is **100% pure Kotlin** with no platform dependencies, meaning the same evaluation and simulation logic runs identically on Android, iOS and Web.

| Component | Description |
|---|---|
| `HandEvaluator` | Evaluates any 5-7 card combination and returns a comparable `HandResult` |
| `MonteCarloSimulator` | Runs N iterations of random board completions to calculate win/tie/loss equity |
| `RangeParser` | Parses standard poker range notation (`AKs`, `QQ+`, `JTs-87s`, etc.) into card combinations |

### Pattern
- **MVI** (Model-View-Intent) with `StateFlow` and `viewModelScope`
- ViewModels in `commonMain` using `androidx.lifecycle.ViewModel`
- Heavy computation dispatched to `Dispatchers.Default` via `withContext` to keep the UI thread free
- Repository pattern with interfaces in `domain` and implementations in `data`

---

##  Tech Stack

| Technology | Usage |
|---|---|
| [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) | Shared business logic across Android, iOS, Web |
| [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) | Shared UI across all platforms |
| [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) | Async computation, StateFlow, viewModelScope |
| [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) | JSON serialization for GTO data |
| [kotlinx.datetime](https://github.com/Kotlin/kotlinx-datetime) | Cross-platform date/time |
| [androidx.lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) | ViewModel + collectAsStateWithLifecycle |
| [Material3](https://m3.material.io/) | UI components and theming |

---

##  Testing

The core engine is fully unit tested with `kotlin.test`:

```
 DeckTest          — 4 tests
 HandEvaluatorTest — 7 tests  
 MonteCarloTest    — 4 tests
 RangeParserTest   — 7 tests
```

Tests run in `commonTest` and execute on all platforms without modification.

Run tests:
```bash
./gradlew :composeApp:testDebugUnitTest
```

---

##  Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17+
- Xcode (iOS builds only — requires macOS)
- Node.js 18+ (Web/Wasm builds)

### Clone & Run

```bash
git clone https://github.com/yourusername/EquimaxLab.git
cd EquimaxLab
```

Open the project in Android Studio and let Gradle sync.

### Run on Android

Select a device or emulator and click **Run ▶** with the `composeApp` configuration.

Recommended emulator: **Pixel 9, API 35+**

For best performance, run on a **physical Android device** — Monte Carlo simulation is significantly faster on real hardware.

### Run on Web (Wasm)

```bash
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

### Run on iOS

Open `iosApp/iosApp.xcodeproj` in Xcode and run on a simulator or device (requires macOS).

---

##  Design System

The app uses a **dark casino aesthetic** with a custom color palette:

| Token | Color | Usage |
|---|---|---|
| `GreenFelt` | `#0D2B1A` | Main background |
| `GreenFeltLight` | `#143622` | Cards, surfaces |
| `GreenAccent` | `#2ECC71` | Primary action, win |
| `GoldAccent` | `#E8C84A` | Hero position, tie |
| `RedAccent` | `#E74C3C` | Error, loss, fold |
| `TextPrimary` | `#ECF0F1` | Main text |
| `TextSecondary` | `#7F9980` | Labels, hints |

---

##  Project Structure

```
EquimaxLab/
├── composeApp/
│   ├── src/
│   │   ├── commonMain/     # Shared code (core + domain + presentation + ui)
│   │   ├── commonTest/     # Unit tests for the core engine
│   │   ├── androidMain/    # Android entry point
│   │   ├── iosMain/        # iOS entry point
│   │   └── wasmJsMain/     # Web entry point
│   └── build.gradle.kts
├── iosApp/                 # Xcode project
├── gradle/
│   └── libs.versions.toml  # Version catalog
└── build.gradle.kts
```

---

##  Roadmap

- [ ] SQLDelight persistence for session history
- [ ] Range builder with visual grid (like PokerStars range editor)
- [ ] Hand history export (PDF / text)
- [ ] Multi-street equity analysis in Replayer
- [ ] Push notifications for daily training reminders
- [ ] iPad / tablet layout

---

##  Author

**Jose** — DAM Student  
Built as a portfolio project to demonstrate Kotlin Multiplatform, Clean Architecture and Compose Multiplatform skills.

---

##  License

```
MIT License — feel free to use, modify and distribute.
```
