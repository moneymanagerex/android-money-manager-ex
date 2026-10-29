# Baseline 01: Core, IoC (Dagger) and EventBus

Questo documento descrive il cuore infrastrutturale di MMEX Android.

## 1. Application Entry Point
- **Classe Principale**: `MmexApplication.java`
- **Responsabilità**: Inizializza il grafo delle dipendenze di Dagger, configura il logging, avvia la migrazione/creazione del database, inizializza le preferenze (`Constants.java`) e fa il setup dei Crash Reports.

## 2. Inversion of Control (Dagger 2)
Il progetto utilizza Dagger classico per l'inizione delle dipendenze in un'architettura monolitica e fortemente accoppiata.

### 2.1 Componenti
- **`core/ioc/MmxComponent`**: Interfaccia principale di Dagger che mappa tutti i punti di iniezione (`inject(...)`) per Activities, Fragments, Services e Worker.

### 2.2 Moduli (`core/ioc/`)
- **`MmxModule`**: Fornisce l'App Context e utility di base globali.
- **`DbModule`**: Si occupa di fornire le istanze `MmxOpenHelper`, `DatabaseManager`, e mappa le URI del `MmxContentProvider`.
- **`RepositoryModule`**: Fornisce in singleton (o per-request) le istanze concrete dei vari Repository presenti in `datalayer/` (es. provvede a ritornare `AccountRepository` quando viene richiesto `IRepository<Account>`).

## 3. Sistema di Logging ed Eccezioni
- Package: `core/log/`
- Pattern: Implementa il framework **Timber** (o logiche affini) tramite astrazioni custom:
  - **`DebugTree`**: Log stampati su Logcat in modalità di sviluppo.
  - **`ScreenTree`**: Sistema custom che intercetta i log rilevanti e li rende visibili all'interno dell'UI dell'app.
  - **`SysLogTree`**: Tracciamento di sistema/file system log.
  - **`ExceptionHandler`**: Cattura eccezioni non gestite (CrashReport) prima che l'app termini.

## 4. Architettura ad Eventi (EventBus)
MMEX fa uso massiccio del pattern Event-Driven (presumibilmente via libreria Greenrobot EventBus o simile) per il decoupling tra UI, Database e Background Services.
- **Pattern**: Classi definite come `*Event.java` raggruppate in `events/` o nei rispettivi package di feature.
- **Flusso Tipico**:
  1. Un Fragment (UI) fa un invio di un comando asincrono e si registra in ascolto.
  2. Un Service in background effettua operazioni di rete o calcolo pesante.
  3. Il Service emette un evento es. `TransactionCreatedEvent(id)`.
  4. L'Activity/Fragment intercetta l'evento tramite un metodo `@Subscribe` e aggiorna l'interfaccia o ricarica il `MmxCursorLoader`.
