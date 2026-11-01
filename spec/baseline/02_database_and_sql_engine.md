# Baseline 02: Database and SQL Engine

This layer represents the true complexity of MMEX Android, as it executes RAW interactions with SQLite, manages encryption, and relies entirely on custom cursors and external files.

## 1. Core SQL Engine
The `database/` and `core/database/` packages handle connections:
- **`MmxOpenHelper.java`**: The main helper managing creation (`onCreate`) via `tables_v1.sql` and updates (`onUpgrade`) by executing raw scripts `database_version_XX.sql` in the exact order.
- **`SupportSQLiteOpenHelperAdapter`**: Acts as a bridge between the standard Android SQLite interface and **SQLCipher** for database encryption.
- **`core/database/DatabaseManager.java`**: Manages the connection pool, checks database availability, and orchestrates thread-safe access.

## 2. Security and Encryption
- **`PasswordActivity`**: Manages the UI for unlocking the database via a master password.
- **`SecureDatabasePasswordManager`**: Keeps the cryptographic keys necessary to instantiate the SQLCipher Helper in secure memory or KeyStore.

## 3. IPC and URI Mapping (Content Provider)
- **`MmxContentProvider.java`**: All UI operations rely on this internal ContentProvider. It intercepts URIs and maps them to the underlying SQLite tables/queries. It releases modification notifications on cursors used by `MmxCursorLoader`.

## 4. Custom Query Architecture (Dataset)
Complex SQL queries (not covered by basic repository CRUD) are modeled as "Dataset" objects.
- **Base Interfaces**: `Dataset`, `DatasetType`.
- **`SQLDataSet`**: Wrapper encapsulating a RAW string (extracted from `res/raw/`) providing methods to bind parameters.
- **`WhereStatementGenerator`**: Utility factory to programmatically compose complex `WHERE` clauses with respective `?` arguments.

### Complex Query Examples (`database/`)
These classes logically map the `res/raw/query_*.sql` files:
- `QueryAllData`: Data search and fusion for comprehensive views.
- `QueryAccountBills`: Calculation of scheduled transaction status and bills related to an account.
- `QueryBillDeposits`: Queries periodic/scheduled transactions.
- `QueryMobileData`: (Used predominantly for sync mapping).
- `QueryReportIncomeVsExpenses`: Extracts aggregated data by month/year for cashflow charts.
- All these queries are read and crossed with metadata defined in `table_config.json`.