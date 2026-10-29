# Baseline 09: Feature - Budgets and Reports

Le funzionalità di pianificazione a lungo termine e reportistica finanziaria.

## 1. Gestione Budget (`budget/`)
MMEX permette di creare budget per categorie su archi temporali mensili o annuali.
- **`BudgetService`**: Classe di orchestrazione. Coordina la logica per accorpare entrate reali contro i limiti impostati.
- **Query Complesse (`BudgetQuery`, `BudgetNestedQuery`)**: Dataset SQL specializzati (spesso unioni tra `BUDGETENTRY_V1`, `CATEGORY_V1` e la `CHECKINGACCOUNT_V1` flitrata per mese).
- **`BudgetPeriods`**: Logica di business per allineare il calendario utente (es. anno finanziario che inizia ad Aprile) con i record SQLite.
- **`BudgetViewModel`**: Fornisce l'astrazione reattiva per il fragment dell'interfaccia utente, iniettata per recuperare lo stato live dei limiti sforati.

## 2. Motore di Reportistica (`reports/`)
I report sono basati sull'esecuzione di pesanti `SQLDataSet` programmatici mappati poi su librerie di grafici esterne (es. MPAndroidChart) o elenchi.

### 2.1 Cashflow e Andamento (`reports/cashflow/`)
- **`CashFlowReport`**: Estrae le entrate e uscite aggregate su una determinata scala temporale (Mese/Anno), sfruttando `QueryReportIncomeVsExpenses`.
- **`IncomeVsExpenses`**: Fragment/Activity che mostra visivamente (tramite bar chart) lo scostamento mensile.

### 2.2 Reportistica Categorie e Beneficiari
- **`CategoriesReport`**: Elenca o rappresenta con grafico a torta (`PieChartFragment`) la distribuzione della spesa per categoria (es. Alimentari 40%, Trasporti 20%).
- **`PayeesReport`**: Estrae le spese raggruppate per entità beneficiaria, rispondendo alla domanda "a chi ho dato più soldi quest'anno?".
- **`SummaryOfAccounts`**: Tabella di sintesi complessiva che estrae il valore totale netto (Net Worth) aggregando asset reali, passività, conti di investimento e liquidità.
