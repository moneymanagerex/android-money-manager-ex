# Change Request (CR) Template - MMEX Android

> **AI Agent Instructions**: Use this rigorous technical template for every modification. The impact analysis must cite `.java` files, `.sql` files in `res/raw/`, and events in `*Event.java`.

---

## Metadata
- **CR ID**: CR-XXX-[Name]
- **Title**: [Technical Title]
- **Status**: Draft / Approved / Implemented
- **Author**: [Agent / Developer]

---

## 1. Objective and Technical Context
*Description of the bug or feature and the business/technical necessity.*

---

## 2. Architectural Impact Analysis (As-Is vs To-Be)

### 2.1 Database and SQL Engine (`res/raw/` & `database/`)
- *Impacted raw SQL files (`query_*.sql`, `tables_v1.sql`)*:
- *Schema modifications (Migration `database_version_XX.sql`)*:
- *Impact on `MmxContentProvider.java` or `MmxOpenHelper.java`*:

### 2.2 Domain Model and Data Layer (`datalayer/` & `domainmodel/`)
- *Impacted `IEntity` / `EntityBase` models*:
- *Impacted Repositories derived from `SqlRepositoryBase`*:
- *`IModificationTraceable` management (Timestamp sync)*:

### 2.3 Core, IoC, and EventBus (`core/ioc/` & `events/`)
- *Modified/created Events (`*Event.java`)*:
- *Impacted Dagger Modules (`MmxModule`, `DbModule`, `RepositoryModule`)*:

### 2.4 Sync Engine and Data Merger (`sync/`)
- *Impact on PocketBase (`PocketBaseSyncEngine.java`) or SyncAdapter*:
- *Conflict resolution logic (`DataMerger.java`)*:

---

## 3. Implementation Plan (Atomic Tasks)
*Sequential, surgical list of tasks with exact filenames to be modified:*
- [ ] Task 1: Modify `res/raw/database_version_XX.sql` to...
- [ ] Task 2: Update model `domainmodel/...` and repository...
- [ ] Task 3: Update EventBus...

---

## 4. Test Plan (Robolectric / Unit)
*Required unit or instrumental tests:*
- Test classes to update (e.g. in `org.moneymanagerex.android.tests` or `TestApplication` override).