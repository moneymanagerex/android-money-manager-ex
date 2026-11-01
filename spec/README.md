# Governance Spec-Driven - MMEX Android

This repository manages the development and refactoring of **Money Manager Ex for Android (MMEX)** via the **Spec-Driven Development (SDD)** protocol.

> **CRITICAL ATTENTION TO REAL ARCHITECTURE**:
> MMEX Android is **NOT** a typical modern app (it doesn't use Room, it doesn't use Kotlin Coroutines/Flow for core data, it doesn't use Hilt). 
> The project is a complex codebase based on **Java**, **classic Dagger IoC**, **EventBus**, custom multi-layer architecture (`datalayer`/`domainmodel`), **Raw SQLite** (`res/raw/*.sql`), `MmxContentProvider`, and custom synchronization engines (classic SyncAdapter and PocketBase).
> Any modification must strictly respect this technology stack.

## 1. Change Lifecycle
1. **Baseline Analysis**: Consult the files in `spec/baseline/` (from `00` to `11`), which document the real Java architecture (classes, packages, SQL, Events) in a granular manner.
2. **Drafting a CR (Change Request)**: Create a file in `spec/cr/` derived from `CR-TEMPLATE.md`. Meticulously analyze the impact on the DB (Raw SQL), Repositories, EventBus, and Sync mechanisms.
3. **Approval & Implementation**: Write code based solely on the atomic tasks defined and approved in the CR, ensuring you implement adequate tests (JUnit/Robolectric).
4. **Baseline Update**: Once the modification is implemented, relevant sections in `spec/baseline/` must be updated to reflect the new classes or tables.

## 2. Directives for the AI Assistant
- Do not assume undocumented technology stacks (no Kotlin Flow unless explicitly requested for new isolated components; respect the use of `EventBus` and `MmxCursorLoader`).
- When defining a task or writing code, always cite the real Java classes and packages (e.g., `com.money.manager.ex.core.database.DatabaseManager`).
- Respect the conflict resolution protocol (`DataMerger.java`) in case of modifications to domain models marked as `IModificationTraceable`.