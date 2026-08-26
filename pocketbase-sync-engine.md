# Technical Documentation: PocketBase Synchronization Implementation

This document describes the technical details of the synchronization implementation between the Android Money Manager Ex application 
and a **PocketBase** instance.

## 1. General Architecture

The synchronization system is designed to be agnostic to the specific structure of the tables, using a JSON configuration file
(`table_config.json`) to drive the mapping and synchronization process.

### Main Components:
- **`PocketBaseSyncEngine`**: The logical core that orchestrates the Push and Pull cycles.
- **`PocketBaseClient`**: Manages the connection, authentication (Dual-mode: User/Superuser), and JWT token refreshing. It uses `EncryptedSharedPreferences` for secure credential storage.
- **`PocketBaseApiService`**: Retrofit interface defining the PocketBase REST endpoints.
- **`table_config.json`**: Configuration file defining which tables to synchronize, the synchronization order, and foreign key (FK) dependencies.

---

## 2. Database Structure and Metadata

To support synchronization, every local table involved in the process must include the following metadata columns:

| Column | Type | Description |
| :--- | :--- | :--- |
| `pb_id` | TEXT | Unique record ID on the PocketBase server. |
| `pb_updated_at` | TEXT | ISO8601 timestamp of the last update on the server. |
| `pb_is_dirty` | INTEGER | Status flag: `0` (Synchronized), `1` (Locally modified), `2` (Downloaded/Pending commit). |

### Deletion Log
A dedicated table `pb_DELETED_RECORDS_LOG` is used to track local deletions while the device is offline, allowing the deletion to be replicated on the server during the next push cycle.

---

## 3. Configuration (`table_config.json`)

The configuration file defines three main sections:
- **`SYNC_CONFIG`**: Maps each table to its Primary Key (`pk`), the fields to be synchronized (`fields`), and uniqueness constraints (`unique`).
- **`SYNC_ORDER`**: The sequential order in which tables are processed (essential for respecting referential integrity constraints).
- **`FK_MAP`**: Defines relationships between tables to allow cascading updates of foreign keys in case of primary key conflicts or re-mapping.

---

## 4. Synchronization Process

The synchronization cycle takes place within an exclusive SQLite transaction and follows two main phases:

### Phase 1: Push (Sending local changes)
1. **Deletions**: Processing records present in `pb_DELETED_RECORDS_LOG` and sending corresponding updates with `_is_deleted=1`.
2. **Creations/Updates**: Searching for records with `pb_is_dirty = 1` or missing `pb_id`.
   - If `pb_id` is missing: `POST` call (Create).
   - If `pb_id` is present: `PATCH` call (Update).
3. **Error and Conflict Handling**:
   - **404 Not Found**: If an update fails because the record has disappeared from the server, a recreation is attempted.
   - **409 Conflict**: If the record has been modified on the server, the remote version is downloaded and applied locally to resolve the conflict.
   - **Unique Constraint Violation**: If the server rejects the insertion due to a uniqueness constraint (e.g., duplicate category name), the engine looks for the corresponding remote record, merges it with the local one, and performs cascading updates on all affected foreign keys (`FK_MAP`) to maintain integrity across the database.

### Phase 2: Pull (Receiving remote changes)
1. Incremental retrieval of records modified after the last successful synchronization timestamp (`_updated_at > lastSyncTime`).
2. **Soft Deletes**: If a record has the `_is_deleted = 1` flag on the server, it is removed from the local database.
3. **Upsert**: If the record exists locally (matched via `pb_id`), it is updated; otherwise, it is inserted as a new record using the server's data.

---

## 5. Security and Authentication

The `PocketBaseClient` implements a two-level authentication system:
1. It first attempts to login as a standard user (`users` collection).
2. If standard login fails, it attempts login as a superuser (`_superusers` collection), allowing using also for administrative, this is useful in self-hosted scenarios.
3. Authentication tokens (JWT) are stored in `EncryptedSharedPreferences`.
4. A dedicated `Interceptor` handles the automatic inclusion of the `Authorization` header in all API requests.

---

## 6. Example Table Configuration

```json
"CATEGORY_V1": {
  "pk": "CATEGID",
  "fields": [
    "CATEGNAME",
    "ACTIVE",
    "PARENTID"
  ],
  "unique": [["CATEGNAME", "PARENTID"]]
}
```
*In this example, the engine identifies CATEGID as the primary key and uses the combination of CATEGNAME and PARENTID to resolve potential duplication conflicts during synchronization.*
**Note:** `"unique":[["field1"],["field2","field3"]]` means that there are two separated unique index, the first one on field "field1", the second on both field "field2"+"field3".
