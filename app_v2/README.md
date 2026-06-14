# MMEX Android V2 Rewrite

## Project Goal
Complete rewrite of the legacy **Money Manager Ex** Android app using **Clean Architecture** principles and a modern technology stack. The objective is to create a solid, maintainable foundation ready for future integrations (e.g., synchronization via PocketBase).

## 1. Isolation Constraints (Strangler Fig Pattern)
- **Untouchable Legacy Code:** New code resides exclusively within the `:app_v2` module.
- **No Reverse Dependencies:** `:app_v2` does not import or refactor code from the old app.
- **Gradual Transition:** New features are implemented from scratch in the new module while the old one remains operational.

## 2. Technology Stack
- **Language:** Kotlin 1.9+ (Idiomatic syntax, Coroutines, Flow).
- **UI:** Jetpack Compose (Material Design 3) - **100% XML-free**.
- **Database:** Room with KSP (strict mapping to the legacy schema).
- **DI:** Hilt (Dagger).
- **Architecture:** MVVM/MVI with Clean Architecture (Domain, Data, Presentation layers).

## 3. Development Status (Vertical Slices)
Development proceeds by complete vertical features.

### Dashboard (Home) - [IN PROGRESS]
- [x] **Account Carousel**: Display of accounts with real balances and forecasts.
- [x] **Summary Cards**: Income/Expense summary with dynamic trends.
- [x] **Period Filter**: Time interval selection (Week, Month, Quarter, etc.).
- [x] **Recent Activity**: List of latest transactions filtered by account.
- [x] **Navigation Drawer**: Database management (Create/Open) and user profile.
- [ ] **Data Integration**: Connection to real Room DAOs (currently based on Fake Repositories).

## 4. Mandatory Architectural Rules
1.  **Data Layer Isolation**: Room entities must never leave the Data layer.
2.  **Mappers**: Mandatory for transforming Entities into Domain Models (DTOs).
3.  **State Hoisting**: Compose components must be Stateless.
4.  **Single Source of Truth**: UI state is managed solely by the `UiState` exposed by the ViewModel via `StateFlow`.

---
*Maintainer: @wolfsolver*
