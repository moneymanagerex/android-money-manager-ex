# Change Request (CR) Template - MMEX Android

> **Istruzioni per l'Agente AI**: Utilizza questo template tecnico rigoroso per ogni modifica. L'analisi d'impatto deve citare i file `.java`, i file `.sql` in `res/raw/` e gli eventi in `*Event.java`.

---

## Metadata
- **CR ID**: CR-XXX-[Nome]
- **Titolo**: [Titolo tecnico]
- **Stato**: Draft / Approved / Implemented
- **Autore**: [Agente / Sviluppatore]

---

## 1. Obiettivo e Contesto Tecnico
*Descrizione del bug o della feature e della necessità di business/tecnica.*

---

## 2. Analisi d'Impatto Architetturale (As-Is vs To-Be)

### 2.1 Database e SQL Engine (`res/raw/` & `database/`)
- *File SQL raw impattati (`query_*.sql`, `tables_v1.sql`)*:
- *Modifiche allo schema (Migration `database_version_XX.sql`)*:
- *Impatto su `MmxContentProvider.java` o `MmxOpenHelper.java`*:

### 2.2 Domain Model e Data Layer (`datalayer/` & `domainmodel/`)
- *Modelli `IEntity` / `EntityBase` impattati*:
- *Repository derivati da `SqlRepositoryBase` toccati*:
- *Gestione `IModificationTraceable` (Timestamp sync)*:

### 2.3 Core, IoC ed EventBus (`core/ioc/` & `events/`)
- *Eventi modificati/creati (`*Event.java`)*:
- *Moduli Dagger (`MmxModule`, `DbModule`, `RepositoryModule`) impattati*:

### 2.4 Sync Engine e Data Merger (`sync/`)
- *Impatto su PocketBase (`PocketBaseSyncEngine.java`) o SyncAdapter*:
- *Logica di risoluzione conflitti (`DataMerger.java`)*:

---

## 3. Piano di Implementazione (Task Atomici)
*Elenco sequenziale e chirurgico dei task con i nomi esatti dei file da modificare:*
- [ ] Task 1: Modifica `res/raw/database_version_XX.sql` per...
- [ ] Task 2: Aggiorna il modello `domainmodel/...` e il repository...
- [ ] Task 3: Aggiorna l'EventBus...

---

## 4. Piano di Test (Robolectric / Unit)
*Test unitari o strumentali richiesti:*
- Classi di test da aggiornare (es. in `org.moneymanagerex.android.tests` o `TestApplication` override).
