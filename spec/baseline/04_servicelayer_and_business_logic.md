# Baseline 04: Service Layer and Business Logic

This document describes the abstraction layer dedicated to business logic and data export, located above the Repositories (`datalayer/`).

## 1. Service Layer Architecture (`servicelayer/`)
The design pattern dictates that fragments and activities almost never call repositories directly, but pass through "Services" that encapsulate application rules (e.g., cascading balance updates, validations, relational integrity checks).

- **`ServiceBase`**: Base class for services. Usually contains references to the context and resolves dependencies via Dagger (injecting the necessary repositories).
- **Injection**: Services are typically instantiated and injected into ViewModels or Fragments via Dagger's `MmxComponent`.

## 2. Core Business Services
- **`AccountService`**: Rules relating to accounts (initial balance calculation, account closure, associated currencies validation).
- **`CategoryService`**: Category tree management logic, checking for orphans or categories in use.
- **`InfoService`**: Database metadata management (schema version, configurations saved in DB).
- **`PayeeService`**: Logic for payee partitioning and normalization.
- **`RecurringTransactionService`**: Application engine for unrolling recurring transactions into actual transactions in the ledger (bills/deposits).
- **`TagService`**: Validation, multiple association of Tags to transactions via the `TAGLINK_V1` bridge table.

## 3. Export and Import Engine
The data exchange logic towards standard formats is isolated from the local DB.

### 3.1 QIF Export (`servicelayer/qif/`)
The Quicken Interchange Format is managed via a custom framework:
- **`QifExport`**: The orchestrator class that retrieves accounts and delegates generation.
- **`QifGenerator`**: Engine that iterates over transaction cursors (using `MmxCursorLoader` or repository queries) and maps the textual data.
- **`QifHeader`**: Models the standard header of a QIF transaction (e.g., `!Type:Bank`).
- **`QifRecord`**: Models individual fields of a record (date, amount, category, payee).

### 3.2 CSV Export
- **`core/ExportToCsvFile.java`**: Robust legacy utility for tabular export of reports or the transaction ledger, accurately handling separator character escaping.