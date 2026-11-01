# Baseline 11: Settings, Security and Widgets

Configurations, biometric security, and integrations on the Android HomeScreen.

## 1. Application Settings (`settings/`)
Configurations are organized in hierarchical and logically divided `SharedPreferences`.
- **Class Hierarchy**: `AppSettings`, `SettingsBase`.
- **Configuration Fragments**: Map the `.xml` files found in `res/xml/`.
  - `preferences_general.xml`: Global settings (theme, language).
  - `preferences_sync.xml` and `SyncPreferences`: Synchronization parameters (pocketbase url, google drive tokens, auto-sync enabled).
- **Per-Database Settings**:
  - `DatabaseSettings` and `PerDatabaseFragment`: Some preferences are not app-global but reside (or are linked) to the currently open `.mmb` database (e.g., default account, base currency). Often data is written to and read from the `INFOTABLE_V1` table.

## 2. Security and Unlock (`passcode/`)
UI protection layer upon application opening (independent of SQLCipher encryption).
- **`PasscodeActivity`**: Lock screen with PIN (Pattern or Numeric keypad) required during the App's `onResume` phase.
- **`FingerprintHandler`**: Integration (often via AndroidX Biometric) that bypasses PIN typing if the user authenticates with fingerprint/FaceID. The handler encapsulates cryptographic callbacks.

## 3. Android Home Widgets (`widget/`)
The app extends core functionalities to the Android launcher for quick access (without opening the app).
All widgets extend `AppWidgetProvider`.

- **`AccountBillsWidgetProvider`**: Shows a brief list of upcoming bills/scheduled expenses on the home screen.
- **`ButtonAddTransactionWidgetProvider`**: Provides a tiny widget (like 1x1) that is simply a button (Shortcut) to directly launch `CheckingTransactionEditActivity` without going through Home.
- **`SingleAccountWidget`**: Uniquely tied to an `AccountID`, it pops up the updated balance of that checking account on screen.
- **`SummaryWidgetProvider`**: A generic widget (e.g., 4x2) that queries the DB in the background, formats the current month's Net Worth and Cashflow, and displays them on the device home. For refresh, they strongly rely on intents (BroadcastReceiver) emitted by `DatabaseManager` in case of data mutations.