# Baseline 05: UI Architecture and Custom Views

This document outlines the User Interface (UI) infrastructure, which combines historical Android patterns with modernized layouts.

## 1. Activity and Fragment Hierarchy
All presentation logic inherits from robust base classes that guarantee consistency (e.g., automatic Loader management, Dagger injection, and theming).

- **`MmxBaseFragmentActivity`**: Main Activity (Appcompat-based). Registers EventBus in `onStart`/`onStop`, prepares the `MmxComponent` (Dagger), and manages the toolbar.
- **List Fragments**:
  - **`AbsListFragment` / `BaseListFragment`**: Legacy fragments based on `ListView`. Manage cursors and display "Empty State" layouts.
  - **`AbsRecyclerFragment` / `BaseRecyclerFragment`**: Modern evolution based on `RecyclerView`. Standard for list screens (Accounts, Categories, Transactions).
- **`AllDataListFragment`**: Abstract base to show complex crossed datasets (`QueryAllData`), implementing generic grouping and sorting logic.

## 2. Cursor Architecture and Adapters
MMEX extensively uses `Loaders` (historical LoaderManager) to query the database in the background and reactively update the UI when the ContentProvider notifies of a change.

- **`MmxCursorLoader`**: Custom loader querying `MmxContentProvider`. When a record is modified elsewhere, the cursor is invalidated, forcing a list refresh.
- **`MoneySimpleCursorAdapter`**: Bridge adapter used in combo boxes (Spinners) and for legacy lists, mapping SQLite columns to XML views.
- **`AllDataAdapter`**: Complex adapter for RecyclerViews, capable of handling multiple ViewTypes (date headers, transactions, totalizers).
- **`*ViewHolder` Pattern**: Each entity has an associated ViewHolder (e.g., `TransactionViewHolder`) that extracts data from the cursor at the current position and binds it to widgets.

## 3. Custom Views and User Interaction (`view/`, `common/`)
- **Custom Font Management (`Roboto*`)**: Classes like `RobotoTextView` or `RobotoButton` that inherit from base widgets dynamically applying the correct typeface (or vector fonts mapped in the `:MMEX_Icon_Font` module).
- **`ContextMenuRecyclerView`**: RecyclerView extension to support classic Android contextual menus on long-press (edit, delete, duplicate).
- **Dialogs and Input**:
  - **`AmountInputDialog`**: High-reliability custom dialog for entering monetary values (comma/dot handling, localization).
  - **`CalculatorActivity`**: Popup activity acting as an integrated calculator, returning the result to the calling view (e.g., transaction form).

*(Note: The introduction of the `viewmodels/` package in some sections gradually supplements or replaces loaders with AndroidX `ViewModel`s, often backed by LiveData or asynchronous calls via EventBus.)*