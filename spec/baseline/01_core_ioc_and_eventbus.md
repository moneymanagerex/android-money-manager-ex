# Baseline 01: Core, IoC (Dagger) and EventBus

This document describes the infrastructural core of MMEX Android.

## 1. Application Entry Point
- **Main Class**: `MmexApplication.java`
- **Responsibilities**: Initializes the Dagger dependency graph, configures logging, starts database migration/creation, initializes preferences (`Constants.java`), and sets up Crash Reports.

## 2. Inversion of Control (Dagger 2)
The project uses classic Dagger for dependency injection in a monolithic and tightly coupled architecture.

### 2.1 Components
- **`core/ioc/MmxComponent`**: Main Dagger interface mapping all injection points (`inject(...)`) for Activities, Fragments, Services, and Workers.

### 2.2 Modules (`core/ioc/`)
- **`MmxModule`**: Provides the App Context and basic global utilities.
- **`DbModule`**: Responsible for providing `MmxOpenHelper`, `DatabaseManager` instances, and mapping the `MmxContentProvider` URIs.
- **`RepositoryModule`**: Provides concrete instances of the various Repositories present in `datalayer/` as singletons (or per-request) (e.g., it provides `AccountRepository` when `IRepository<Account>` is requested).

## 3. Logging and Exceptions System
- Package: `core/log/`
- Pattern: Implements the **Timber** framework (or similar logic) via custom abstractions:
  - **`DebugTree`**: Logs printed to Logcat in development mode.
  - **`ScreenTree`**: Custom system that intercepts relevant logs and makes them visible within the app's UI.
  - **`SysLogTree`**: System tracking / file system logs.
  - **`ExceptionHandler`**: Catches unhandled exceptions (CrashReport) before the app terminates.

## 4. Event Architecture (EventBus)
MMEX heavily uses the Event-Driven pattern (presumably via Greenrobot EventBus library or similar) for decoupling UI, Database, and Background Services.
- **Pattern**: Classes defined as `*Event.java` grouped in `events/` or in respective feature packages.
- **Typical Flow**:
  1. A Fragment (UI) sends an asynchronous command and registers as a listener.
  2. A Background Service performs network operations or heavy computations.
  3. The Service emits an event e.g., `TransactionCreatedEvent(id)`.
  4. The Activity/Fragment intercepts the event via a `@Subscribe` method and updates the interface or reloads the `MmxCursorLoader`.