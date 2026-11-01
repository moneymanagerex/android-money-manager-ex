# Baseline 07: Feature - Accounts and Transactions

This document describes the flow of MMEX's main features, mapping the user entry from the app to the insertion of a transaction and the asynchronous recalculation of balances.

## 1. App Entry and Dashboard (`home/`, `search/`)
- **`MainActivity`**: The main container host with the Navigation Drawer.
- **`HomeFragment` / `DashboardFragment`**: Landing points. They summarize the net worth by aggregating checking accounts (Assets) net of liabilities.
- **Search (`search/`)**:
  - **`SearchParameters`**: Model collecting user filters (amount, date, tag, note, status, payee). Passed to the `Dataset` which dynamically generates the `WhereStatementGenerator`.

## 2. Checking Accounts Flow (`account/`)
- The list is served by `AbsRecyclerFragment` hooked to `AccountRepository`.
- **Balance Recalculation (`CalculateRunningBalanceTask2`)**: A crucial asynchronous task (historically `AsyncTask` or worker) that recomputes an account's current balance by iterating over its entire transaction ledger from the "Initial Balance". It does not rely on SQL triggers but on Java application logic, in order to recalculate progressive balances row by row (Running Balance).

## 3. Transactions Flow (`transactions/`)
- **Modification and Insertion**:
  - **`CheckingTransactionEditActivity`**: The complex transaction edit Activity (embeds Input Dialog for amount, Spinners for categories/payee, DatePicker).
  - **`EditTransactionCommonFunctions`**: Helper class abstracting shared validations and logic (e.g., non-null amount validation or currency exchange handling for international transfers).
- **Nested Categories Management (`nestedcategory/`)**:
  - MMEX does not have separate tables for sub-categories; it uses ParentID. `NestedCategoryEntity` and `QueryNestedCategory` complete the tree (Parent > Child), displaying it in the UI spinner.
- **Tag Flow (`tag/`)**:
  - M2M (Many-To-Many) implementation. An interface (`TagLinkUtils`) allows displaying a text field with Tags (comma-separated), which are parsed and translated into queries on the `TAGLINK_V1` join table during transaction saving.

## 4. Split Transactions (`SplitCategoriesActivity`)
A single grocery expense can be categorized into multiple branches (Food, Detergents).
- It is mapped via the `SPLITTRANSACTIONS_V1` table.
- The `SplitCategoriesActivity` handles a special adapter where the user adds N rows. The sum of the Split rows must match the total amount of the parent `AccountTransaction`, otherwise an imbalance warning is shown and saving is blocked.