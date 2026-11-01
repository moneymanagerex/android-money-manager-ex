# Baseline 09: Feature - Budgets and Reports

Long-term planning and financial reporting functionalities.

## 1. Budget Management (`budget/`)
MMEX allows creating category budgets over monthly or yearly timeframes.
- **`BudgetService`**: Orchestration class. Coordinates the logic to aggregate actual expenses against set limits.
- **Complex Queries (`BudgetQuery`, `BudgetNestedQuery`)**: Specialized SQL datasets (often joins between `BUDGETENTRY_V1`, `CATEGORY_V1`, and `CHECKINGACCOUNT_V1` filtered by month).
- **`BudgetPeriods`**: Business logic to align the user calendar (e.g., financial year starting in April) with SQLite records.
- **`BudgetViewModel`**: Provides the reactive abstraction for the user interface fragment, injected to retrieve the live status of exceeded limits.

## 2. Reporting Engine (`reports/`)
Reports are based on the execution of heavy programmatic `SQLDataSet`s mapped to external charting libraries (e.g., MPAndroidChart) or lists.

### 2.1 Cashflow and Trend (`reports/cashflow/`)
- **`CashFlowReport`**: Extracts aggregated income and expenses over a specific time scale (Month/Year), utilizing `QueryReportIncomeVsExpenses`.
- **`IncomeVsExpenses`**: Fragment/Activity that visually shows (via bar chart) the monthly variance.

### 2.2 Category and Payee Reporting
- **`CategoriesReport`**: Lists or graphically represents (via `PieChartFragment`) the distribution of spending by category (e.g., Groceries 40%, Transport 20%).
- **`PayeesReport`**: Extracts expenses grouped by payee entity, answering the question "who did I give the most money to this year?".
- **`SummaryOfAccounts`**: Overall summary table that extracts the total Net Worth aggregating real assets, liabilities, investment accounts, and liquidity.