# Baseline 03: Data Layer and Domain Models

L'astrazione dei dati per l'CRUD tabellare base usa un ORM interno custom basato su astrazioni Repository ed Entity. Non usa librerie standard di mercato (es. Room).

## 1. Architettura Data Layer (`datalayer/`)
- **`IRepository<T extends IEntity>`**: Interfaccia generica per operazioni CRUD.
- **`RepositoryBase`**: Classe astratta che gestisce operazioni comuni sui repository.
- **`SqlRepositoryBase`**: L'implementazione concreta di base. Traduce gli oggetti di dominio in `ContentValues` ed esegue insert, update e map dei Cursori ad oggetti.
- **`Select`**: Una classe builder/fluent usata nel layer di business per comporre query programmatiche (`Select(Tabella).where(campo).eq(valore).execute()`).

## 2. Modelli di Dominio (`domainmodel/`)
- **`IEntity`**: Interfaccia contrattuale per tutti gli oggetti salvabili.
- **`EntityBase`**: Provvede getter/setter per le PK.
- **`IModificationTraceable`**: Interfaccia **fondamentale per la Sincronizzazione**. Contiene metodi per leggere/scrivere `UPDATEDTIME` e `CREATEDTIME`.
- **`RefType`**: Enum usata frequentemente per indicare se una riga si riferisce a un Trasferimento, Entrata, Uscita ecc.

## 3. Mappa Completa Modelli, Repository e Tabelle
Qui la mappa esatta tra i 23 Modelli di Dominio, i rispettivi Repository (iniettati via Dagger) e il nome tabella fisico SQLite (dichiarato nello schema).

| Domain Model | Repository in `datalayer/` | Tabella SQLite RAW (`tables_v1.sql`) |
|--------------|---------------------------|----------------------------------|
| `Account` | `AccountRepository` | `ACCOUNTLIST_V1` |
| `AccountTransaction` | `AccountTransactionRepository` | `CHECKINGACCOUNT_V1` |
| `Asset` | `AssetRepository` | `ASSETS_V1` |
| `Attachment` | `AttachmentRepository` | `ATTACHMENT_V1` |
| `Budget` | `BudgetRepository` | `BUDGETTABLE_V1` |
| `BudgetEntry` | `BudgetEntryRepository` | `BUDGETENTRY_V1` |
| `Category` | `CategoryRepository` | `CATEGORY_V1` |
| `Currency` | `CurrencyRepository` | `CURRENCYFORMAT_V1` |
| `CurrencyHistory` | `CurrencyHistoryRepository` | `CURRENCYHISTORY_V1` |
| `CustomField` | `CustomFieldRepository` | `CUSTOMFIELD_V1` |
| `CustomFieldData` | `CustomFieldDataRepository`| `CUSTOMFIELDDATA_V1` |
| `Info` | `InfoRepository` | `INFOTABLE_V1` |
| `Payee` | `PayeeRepository` | `PAYEE_V1` |
| `RecurringTransaction`| `RecurringTransactionRepository` | `BILLSDEPOSITS_V1` |
| `Report` | `ReportRepository` | `REPORT_V1` |
| `ShareInfo` | `ShareInfoRepository` | `SHAREINFO_V1` |
| `SplitCategory` | `SplitCategoryRepository` | `SPLITTRANSACTIONS_V1` |
| `SplitRecurringCategory`| `SplitRecurringCategoryRepository` | `SPLITBILLS_V1` |
| `Stock` | `StockRepository` | `STOCK_V1` |
| `StockHistory` | `StockHistoryRepository` | `STOCKHISTORY_V1` |
| `Tag` | `TagRepository` | `TAG_V1` |
| `TagLink` | `TagLinkRepository` | `TAGLINK_V1` |
| `TransactionLink` | `TransactionLinkRepository` | `TRANSACTIONLINK_V1` |

> *Nota Architetturale: Qualsiasi modifica ai campi dei Domain Models (es. `Category.java`) implica una modifica parallela nella logica di ContentValue mapping all'interno del relativo `CategoryRepository` e un'alterazione dello schema in un nuovo file `database_version_XX.sql` in `res/raw/`.*
