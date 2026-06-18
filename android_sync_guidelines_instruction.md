# Synchronization Implementation Guidelines for the Android App (AMMEX Sync)

This document describes the technical requirements, use cases, and business rules that the Android application (AMMEX) must implement in its database and synchronization services to correctly integrate with the PocketBase backend and maintain full data consistency with the Desktop version.

---

## 1. Database Structure and Three-State Protocol

To avoid infinite synchronization loops and correctly track data state in an "offline-first" mode, every synchronized table in the local SQLite database of the Android device must contain three additional technical columns:

1. **`pb_id` (TEXT):** The unique identifier of the record on the PocketBase server (null or empty for records created offline and not yet synchronized).


2. **`pb_is_dirty` (INTEGER):** Indicates the local synchronization state of the record:


* **`0` (Synced):** The local record is aligned with the server.


* **`1` (Local Change):** The record has been inserted or modified locally and must be sent to the server.


* **`2` (Cloud Ingress):** Temporary state. Indicates that the record is being updated/inserted as a result of a remote synchronization operation (Pull). This state disables local triggers to prevent infinite loops.




3. **`pb_updated_at` (TEXT):** Timestamp of the last update (in ISO-8601 UTC format).



### Local SQLite Triggers on Android

The Android app must define SQLite triggers for each table equivalent to those on the desktop version:

* **INSERT Trigger:** When a record is inserted with `pb_is_dirty != 2`, it automatically sets `pb_is_dirty = 1` and populates `pb_updated_at` with the current time.


* **UPDATE Trigger:** When a non-technical column is modified and `pb_is_dirty != 2`, it automatically sets `pb_is_dirty = 1` and updates `pb_updated_at`.


* **DELETE Trigger:** Before deleting a record that has a non-null `pb_id`, it inserts a row containing the table name and the `pb_id` into the technical log table `pb_DELETED_RECORDS_LOG`.



---

## 2. Synchronization Ordering (`SYNC_ORDER`)

To prevent Foreign Key constraint violations both in the local SQLite database and in PocketBase collections, tables must be processed strictly in sequence (in ascending order for both **Push** and **Pull**, ensuring that dependent tables are updated only after their parent tables):

```markdown
1.  INFOTABLE_V1
2.  CURRENCYFORMATS_V1
3.  TAG_V1
4.  BUDGETYEAR_V1
5.  CUSTOMFIELD_V1
6.  CATEGORY_V1 (Self-referential: handle the PARENTID hierarchy with care)
7.  PAYEE_V1
8.  ACCOUNTLIST_V1
9.  ASSETS_V1
10. STOCK_V1
11. CHECKINGACCOUNT_V1
12. BILLSDEPOSITS_V1
13. BUDGETTABLE_V1
14. SPLITTRANSACTIONS_V1
15. BUDGETSPLITTRANSACTIONS_V1
16. CURRENCYHISTORY_V1
17. STOCKHISTORY_V1
18. ATTACHMENT_V1
19. TAGLINK_V1
20. TRANSLINK_V1
21. SHAREINFO_V1
22. CUSTOMFIELDDATA_V1

```

(Source: derived from table order specifications)

---

## 3. PUSH Flow Management Protocol (Local $\rightarrow$ Server)

The Push phase sends all local changes accumulated offline to the PocketBase server. For each table (in the order defined in section 2), extract all records that have `pb_is_dirty = 1` or lack a `pb_id`, and process each row according to the logic defined below:

### Scenario A: Empty `pb_id` (Locally created record)

1. **Submission:** Perform a `CREATE` request on the PocketBase server.


2. **Successful Outcome (OK):** Save the `pb_id` returned by the server into the local record and set `pb_is_dirty = 0`.


3. **Unsuccessful Outcome (KO):** If the creation fails due to a primary key (PK) violation or a uniqueness (Unique) constraint:


* If the error returned by the server is `validation_not_unique`, extract the list of fields involved in the uniqueness violation (e.g., `REFTYPE`, `REFID`, `TAGID` for `TAGLINK_V1`). Otherwise, refer to the primary key (PK) of the record.


* Execute a search query on the PocketBase server to find a remote record with the same PK or with the same value for the extracted Unique fields.


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

The pull phase retrieves modifications made on other devices and aligns the local DB.

1. **Incremental Filter (lastSync):** To optimize performance, request from the server only those records modified after the timestamp of the last successful synchronization (`_updated_at > lastSync`). Subtract a safety tolerance window of approximately 5 seconds (`lastSync - 5s`) to avoid missing concurrent writes.


2. **Forced Synchronization (Force Mode):** If explicitly requested (or upon initial startup), ignore the time filter and download the entire list of records.


3. **Applying Received Records:**
* **If the remote record does NOT exist locally:**
* If it is marked on the server as deleted (`_is_deleted != 0`), ignore it.


* Otherwise, insert it into the local DB by temporarily setting `pb_is_dirty = 2` (to deactivate local triggers), write the data, and then set `pb_is_dirty = 0`.




* **If the remote record ALREADY exists locally (same `pb_id`):**
* If it is marked on the server as deleted (`_is_deleted != 0`), proceed to physically delete the record from the local database.


* Otherwise, update all local fields with the remote values, setting `pb_is_dirty = 2` during the update and resetting it to `0` once the transaction is finalized.







---

## 5. Local Deletion Management (DELETE $\rightarrow$ Server)

When the user deletes data from the offline Android interface:

1. The local deletion populates the `pb_DELETED_RECORDS_LOG` table via the `TRG_DELETE` trigger.


2. During the synchronization phase, send a `DELETE` request to the server for each `PB_ID` present in the deletion log.


3. **Handling 404 on Delete:** If the server returns a 404 error (the record has already been deleted on the server by another device), the error should be ignored.


4. Entirely clear the local `pb_DELETED_RECORDS_LOG` table upon successful completion of the operation.



---

## 6. Specifications and Considerations for Android Development (Kotlin/Java)

* **SQLite Transaction Management:** All local database write operations during the execution of Pull or Push phases must be wrapped inside SQLite transactions (`db.beginTransaction()` / `db.endTransaction()`) to guarantee atomicity and high performance.


* **Trigger Management in Room / SQLite Open Helper:** If using Jetpack's Room library, note that schema changes (adding technical columns and triggers) must be handled via explicit Migrations. Triggers must be explicitly recreated when opening the database (`RoomDatabase.Callback.onOpen`).


* **Deactivating Triggers (State 2):** Ensure that functions writing to the local DB during the pull phase strictly utilize the `pb_is_dirty = 2` state logic before proceeding with the insert/update operation. Otherwise, constraint violation crashes or infinite loops updating local timestamps will occur.


* **Recovery in Case of Failure:** If a synchronization is interrupted (e.g., sudden loss of connectivity), implement a `try-finally` block or a startup recovery mechanism that resets all local records left stuck in state `2` back to state `1`.


* **Time Zone Management:** All timestamps exchanged with PocketBase must be stored in UTC ISO-8601 format (e.g., `yyyy-MM-dd'T'HH:mm:ss'Z'`).