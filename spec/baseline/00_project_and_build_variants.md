# Baseline 00: Project and Build Variants

## 1. Source Sets Structure (`app/src/`)
The main module `:app` manages different build variants and flavors, orchestrated via Gradle. The source tree reflects a deep separation between distribution targets and test suites.

### 1.1 Distribution Variants (Flavors / Build Types)
- **`app/src/main/`**: The core of the MMEX application containing most of the Java code, UI resources, and the main Manifest configuration.
- **`app/src/fdroid/`**: Specific variant for F-Droid release, without proprietary Google Play Services dependencies (e.g., removal of proprietary tracking or Google Drive sync).
- **`app/src/beta/`**: No longer used.

### 1.2 Test Variants
- **`app/src/test/`**: Unit tests executed on the local JVM (JUnit 4). Focus on business calculations and Android-decoupled logic.
- **`app/src/androidTest/`**: Instrumental tests and **Robolectric** based tests. Includes dedicated configurations in the `org.moneymanagerex.android.tests` package.
  - **`TestApplication`**: Subclass of the application used during tests to inject mocks or an in-memory database, skipping standard Dagger initialization (MmxComponent).

**Note**: The entire test structure has been partially updated and does not cover all functionality.

---

## 2. Assets and Raw SQL Management (`app/src/main/`)

Unlike apps using an ORM, MMEX uses manual and highly structured management for assets and SQL queries.

### 2.1 Assets (`src/main/assets/`)
- **`fonts/`**: Contains custom typography and the MMEX icon-font (shared/linked with `:MMEX_Icon_Font`).
- **`tutorial_images/`**: Images for the initial onboarding carousel or introductory guide.

### 2.2 Raw SQL (`src/main/res/raw/`)
This directory is the heart of database interaction.
- **Schema Setup File**: Contains `tables_v1.sql` (the complete initial schema).
- **Migration Files**: Series of files from `database_version_0.sql` to `database_version_21.sql` (or later) used by `MmxOpenHelper` for update deltas.
- **Query Files (`query_*.sql`)**: Complex multi-table queries loaded at runtime for reporting and the dashboard (e.g., cross-account balance).
- **`table_config.json`**: Descriptor file that maps the structure of tables and associated metadata.