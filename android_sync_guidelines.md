# Synchronization Specifications for the Android Environment (AMMEX Sync)

This document provides detailed technical specifications to implement "Offline-First" synchronization with PocketBase in the Android application (AMMEX), ensuring data consistency with the Desktop client.

---

## 1. Three-State Logic (`pb_is_dirty`)

To avoid infinite synchronization loops and track offline modifications, every synchronized table in the local SQLite database must include three technical columns:

1. **`pb_id` (TEXT):** The unique identifier of the remote record on PocketBase.
2. **`pb_is_dirty` (INTEGER):** The state of the local record:
* **`0` (Synced):** The local record is aligned with the server.
* **`1` (Local Change):** The record has been modified/inserted offline and must be sent to the server.
* **`2` (Cloud Ingress):** Temporarily disables triggers to prevent loops during network writes.


3. **`pb_updated_at` (TEXT):** UTC date/time of the last modification (ISO-8601 format).

### Local SQLite Triggers on Android

Each synchronized table must have local triggers:

* **INSERT:** If `pb_is_dirty != 2`, automatically set `pb_is_dirty = 1` and populate `pb_updated_at`.
* **UPDATE:** If `pb_is_dirty != 2` and a non-technical field is modified, set `pb_is_dirty = 1` and update `pb_updated_at`.
* **DELETE:** Before physically deleting a record with a non-null `pb_id`, insert the table name and `pb_id` into the technical table `pb_DELETED_RECORDS_LOG`.

---

## 2. Table Ordering (`SYNC_ORDER`)

To prevent referential integrity errors (Foreign Key constraints), tables must be synchronized in the order specified in [table_config.js](https://www.google.com/search?q=file:///c:/Users/EmmanuelePrudenzano/StudioProjects/MMEX-Sync/mmex-sync/src/config/table_config.js):

1. **Level 1 (Independent):** `INFOTABLE_V1`, `CURRENCYFORMATS_V1`, `TAG_V1`, `BUDGETYEAR_V1`, `CUSTOMFIELD_V1`.
2. **Level 2 (Master Data):** `CATEGORY_V1` (pay attention to the `PARENTID` hierarchy), `PAYEE_V1`, `ACCOUNTLIST_V1`.
3. **Level 3 (Entities and Movements):** `ASSETS_V1`, `STOCK_V1`, `CHECKINGACCOUNT_V1`, `BILLSDEPOSITS_V1`, `BUDGETTABLE_V1`.
4. **Level 4 (Details and Links):** `SPLITTRANSACTIONS_V1`, `BUDGETSPLITTRANSACTIONS_V1`, `CURRENCYHISTORY_V1`, `STOCKHISTORY_V1`, `ATTACHMENT_V1`, `TAGLINK_V1`, `TRANSLINK_V1`, `SHAREINFO_V1`, `CUSTOMFIELDDATA_V1`.

---

## 3. PUSH Flow Management Protocol (Local $\rightarrow$ Server)

The Push phase sends all local changes accumulated offline to the PocketBase server. For each table (in the order defined in section 2), extract all records that have `pb_is_dirty = 1` or lack a `pb_id`, and process each row according to the logic defined below:

### Scenario A: Empty `pb_id` (Locally created record)

1. **Submission:** Perform a `CREATE` request on the PocketBase server.
2. **Successful Outcome (OK):** Save the `pb_id` returned by the server into the local record and set `pb_is_dirty = 0`.
3. **Unsuccessful Outcome (KO):** If the creation fails due to a primary key (PK) violation or a uniqueness (Unique) constraint:
* If the error returned by the server is `validation_not_unique`, extract the list of fields involved in the uniqueness violation (e.g., `REFTYPE`, `REFID`, `TAGID` for `TAGLINK_V1`). Otherwise, refer to the primary key (PK) of the record.
* Execute a search query on the PocketBase server to find a remote record with the same PK or with the same values for the extracted Unique fields.
* **If the remote record is found:** Resolve the conflict by replacing the local record with the remote one. Physically delete the local record with the original key and insert the remote record, correctly populating the `pb_id`, connected columns, and setting `pb_is_dirty = 0`.



### Scenario B: Non-Empty `pb_id` (Previously synchronized record)

1. **Submission:** Perform an `UPDATE` request on the PocketBase server for the corresponding `pb_id`.
2. **Successful Outcome (OK):** Set `pb_is_dirty = 0` in the local database.
3. **Unsuccessful Outcome (KO):** If the update fails, it means the record was deleted from the server (404 error).
* **Resolution:** Attempt to recreate the record on the PocketBase server via a `CREATE` call, explicitly passing the original local `pb_id` (which is no longer present on the server).
* **If the `CREATE` succeeds:** Set `pb_is_dirty = 0` on the local record.
* **If the `CREATE` fails:** It means the operation runs into a uniqueness constraint. Use the same mechanism described in *Scenario A* (KO) to identify the PK or Unique fields, search for the record on the server, and perform a local replacement of the record with the remote one.



### Handling Temporal Conflicts (409 Error)

In any `UPDATE` or `CREATE` call (in case the server handles row version control), the server might return a **409 Conflict** error, indicating that the version of the record on the server is newer than the local one.

* **Resolution:** Ignore local changes on the current record, download the updated record from the server (e.g., via `getById`), update the local database with the remote data, and set the record state to `0` (synchronized).

---

## 4. PULL Flow Management (Server $\rightarrow$ Local)

1. **Incremental Filter (lastSync):** Request only the records modified after the last completed sync (`_updated_at > lastSync`), subtracting a safety window of 5 seconds (`lastSync - 5s`).
2. **Forced Synchronization (Force Mode):** Ignore the timestamp and download all data.
3. **Applying Changes:**
* **If the remote record does NOT exist locally:** If `_is_deleted = 0`, insert it into the local DB by temporarily setting `pb_is_dirty = 2`, write the data, and then update it to `0`.
* **If the remote record ALREADY exists locally:**
* If `_is_deleted != 0` on the server, physically delete the local record.
* Otherwise, update all local fields with the server values, setting `pb_is_dirty = 2` during the write process, and finally set it to `0`.





---

## 5. Local Deletion Management (DELETE $\rightarrow$ Server)

1. During synchronization, extract all rows from the local technical table `pb_DELETED_RECORDS_LOG`.
2. For each row, send a `DELETE` request to the PocketBase server using the recorded `PB_ID`.
3. If the request returns **404 (Not Found)**, ignore the error (the record had already been removed).
4. Once the cycle is complete for all rows, entirely clear the `pb_DELETED_RECORDS_LOG` table.

---

## 6. Specifications and Considerations for Android Development (Kotlin/Java)

* **Transactions:** Wrap local database writes during the Pull phase inside SQLite transactions to preserve consistency and performance.
* **Trigger Initialization in Room/SQLite:** Ensure that technical triggers are correctly recreated during schema migration or database opening.
* **Thread Safety and Connection:** Protect the synchronization process from overlapping concurrent calls (e.g., using a mutex or a service-level state flag).
* **Crash Recovery:** In the event of a crash or disconnection during synchronization, reset any records left in state `2` back to state `1` upon the next startup.