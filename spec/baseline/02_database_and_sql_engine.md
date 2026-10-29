# Baseline 02: Database and SQL Engine

Questo strato rappresenta la vera complessità di MMEX Android, in quanto esegue interazioni RAW con SQLite, gestisce crittografia e si appoggia interamente a cursori custom e file esterni.

## 1. Core SQL Engine
Il package `database/` e `core/database/` gestiscono le connessioni:
- **`MmxOpenHelper.java`**: L'helper principale che gestisce la creazione (`onCreate`) tramite `tables_v1.sql` e l'aggiornamento (`onUpgrade`) eseguendo gli script raw `database_version_XX.sql` nell'ordine esatto.
- **`SupportSQLiteOpenHelperAdapter`**: Fa da bridge tra l'interfaccia SQLite standard di Android e **SQLCipher** per la crittografia del database.
- **`core/database/DatabaseManager.java`**: Gestisce il pool di connessioni, controlla la disponibilità del database e orchestra l'accesso thread-safe.

## 2. Sicurezza e Crittografia
- **`PasswordActivity`**: Gestisce l'UI per lo sblocco del database tramite master password.
- **`SecureDatabasePasswordManager`**: Mantiene in memoria sicura o KeyStore le chiavi crittografiche necessarie per istanziare l'Helper di SQLCipher.

## 3. IPC e URI Mapping (Content Provider)
- **`MmxContentProvider.java`**: Tutte le operazioni UI si appoggiano a questo ContentProvider interno. Intercetta le URI e le mappa sulle tabelle/query SQLite sottostanti. Rilascia notifiche di modifica sui cursori usati da `MmxCursorLoader`.

## 4. Architettura delle Query Custom (Dataset)
Le interrogazioni SQL complesse (non coperte dal CRUD base dei repository) sono modellate come oggetti "Dataset".
- **Interfacce Base**: `Dataset`, `DatasetType`.
- **`SQLDataSet`**: Wrapper che accorpa una stringa RAW (estratta da `res/raw/`) fornendo metodi per legare i parametri.
- **`WhereStatementGenerator`**: Utility factory per comporre programmaticamente le clausole `WHERE` complesse con i rispettivi argomenti `?`.

### Esempi di Query Complesse (`database/`)
Queste classi mappano logicamente i file `res/raw/query_*.sql`:
- `QueryAllData`: Ricerca e fusione dati per visualizzazioni omnicomprensive.
- `QueryAccountBills`: Calcolo stato delle scadenze e bollette relative a un conto.
- `QueryBillDeposits`: Interroga transazioni periodiche/programmate.
- `QueryMobileData`: (Usata prevalentemente per mapping sync).
- `QueryReportIncomeVsExpenses`: Estrae i dati aggregati per mese/anno per i grafici cashflow.
- Tutte queste query vengono lette e incrociate con i metadati definiti in `table_config.json`.
