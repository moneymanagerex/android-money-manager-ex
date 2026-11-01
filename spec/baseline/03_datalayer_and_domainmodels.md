# Baseline 03: Data Layer and Domain Models

The data abstraction for basic tabular CRUD uses a custom internal ORM based on Repository and Entity abstractions. It does not use standard market libraries (e.g., Room).

## 1. Data Layer Architecture (`datalayer/`)
- **`IRepository<T extends IEntity>`**: Generic interface for CRUD operations.
- **`RepositoryBase`**: Abstract class handling common repository operations.
- **`SqlRepositoryBase`**: The concrete base implementation. Translates domain objects into `ContentValues` and executes inserts, updates, and maps Cursors to objects.
- **`Select`**: A builder/fluent class used in the business layer to programmatically compose queries (`Select(Table).where(field).eq(value).execute()`).

## 2. Domain Models (`domainmodel/`)
- **`IEntity`**: Contract interface for all savable objects.
- **`EntityBase`**: Provides getters/setters for PKs.
- **`IModificationTraceable`**: **Fundamental interface for Synchronization**. Contains methods to read/write `UPDATEDTIME` and `CREATEDTIME`.
- **`RefType`**: Enum frequently used to indicate if a row refers to a Transfer, Deposit, Withdrawal, etc.

## 3. Complete Map of Models, Repositories, and Tables
Here is the exact map between the 23 Domain Models, their respective Repositories (injected via Dagger), and the physical SQLite table name (declared in the schema).

| Domain Model | Repository in `datalayer/` | RAW SQLite Table (`tables_v1.sql`) |
|--------------|---------------------------|----------------------------------|
| `Account` | `AccountRepository` | `ACCOUNTLIST_V1` |
| `AccountTransaction` | `AccountTransactionRepository` | `CHECKINGACCOUNT_V1` |
| `Asset` | `AssetRepository` | `ASSETS_V1` |
| `Attachment` | `AttachmentRepository` | `ATTACHMENT_V1` |
| `Budget` | `BudgetRepository` | `BUDGETTABLE_V1` |
| `BudgetEntry` | `BudgetEntryRepository` | `BUDGETENTRY_V1` |
| `Category` | `CategoryRepository` | `CATEGORY_V1` |
| `Currency` | `CurrencyRepository` | `CURRENCYFORMAT_V1` |
| `CurrencyHistory` | `CurrencyHistoryRepository` | `CURRENCYHISTORY_V1` |
| `CustomField` | `CustomFieldRepository` | `CUSTOMFIELD_V1` |
| `CustomFieldData` | `CustomFieldDataRepository`| `CUSTOMFIELDDATA_V1` |
| `Info` | `InfoRepository` | `INFOTABLE_V1` |
| `Payee` | `PayeeRepository` | `PAYEE_V1` |
| `RecurringTransaction`| `RecurringTransactionRepository` | `BILLSDEPOSITS_V1` |
| `Report` | `ReportRepository` | `REPORT_V1` |
| `ShareInfo` | `ShareInfoRepository` | `SHAREINFO_V1` |
| `SplitCategory` | `SplitCategoryRepository` | `SPLITTRANSACTIONS_V1` |
| `SplitRecurringCategory`| `SplitRecurringCategoryRepository` | `SPLITBILLS_V1` |
| `Stock` | `StockRepository` | `STOCK_V1` |
| `StockHistory` | `StockHistoryRepository` | `STOCKHISTORY_V1` |
| `Tag` | `TagRepository` | `TAG_V1` |
| `TagLink` | `TagLinkRepository` | `TAGLINK_V1` |
| `TransactionLink` | `TransactionLinkRepository` | `TRANSACTIONLINK_V1` |

> *Architectural Note: Any modification to the fields of Domain Models (e.g., `Category.java`) implies a parallel modification in the ContentValue mapping logic within the relevant `CategoryRepository` and a schema alteration in a new `database_version_XX.sql` file in `res/raw/`.*