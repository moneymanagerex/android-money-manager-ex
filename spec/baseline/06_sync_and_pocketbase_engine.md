# Baseline 06: Sync and PocketBase Engine

Synchronization in MMEX Android is one of the most critical and complex features, essential for maintaining alignment with the Desktop client. It supports two distinct architectural strategies (classic and PocketBase).

## 1. Classic Sync Engine (SyncAdapter & Workers)
Initially based on the Android SyncAdapter pattern and later migrated/supported by WorkManager.

- **`SyncManager` / `SyncService`**: Historical entry points of the sync (often linked to Google Drive or Dropbox).
- **`SyncAdapter`**: Abstract Android thread for aggregate periodic synchronizations.
- **WorkManager (`sync/jobmanager/`)**:
  - **`DownloadWorker` / `UploadWorker`**: Background tasks that download the remote MMEX database (e.g., Cloud) and upload local differences.
  - **`SyncWorker`**: Unified job orchestrating the entire Push/Pull cycle.

## 2. PocketBase Sync Engine (`sync/pocketbase/`)
A modern synchronization engine based on REST APIs towards a **PocketBase** backend. Unlike full-file sync, PocketBase synchronizes **individual records**.

- **`PocketBaseClient`**: Initializes the client and manages the JWT authentication token.
- **`PocketBaseApiService`**: Retrofit (or custom HTTP client) interfaces for CRUD calls to PocketBase collections.
- **`PocketBaseSyncEngine`**: Orchestrator that queries the `/api/collections/...` endpoint and applies push/pull of records based on hash and date.
- **`PocketBaseSetupActivity`**: UI for entering the user's server URL, email, and password.

## 3. Data Merger and Conflict Resolution (`sync/merge/`)
The nerve center for merging remote DBs (files or records) with the local DB.

- **Timestamp & Traceability**: Crucial use of the `IModificationTraceable` interface. The `UPDATEDTIME` fields determine which version wins in case of conflict.
- **`DataMerger.java`**: Engine that iterates table by table (Account, Payee, Transaction), comparing records. Supports Insert, Update, and Soft-Delete operations.
- **`MergeConflictResolution.java`**: Resolution logic (LWW - Last Write Wins, default base).
- **Events in `sync/events/`**: The sync engine notifies the UI of progress by emitting events on the EventBus (e.g., `SyncProgressEvent`, `SyncCompletedEvent`), allowing list `MmxCursorLoader`s to invalidate and reload updated data without user intervention.