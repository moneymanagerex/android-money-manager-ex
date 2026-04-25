# Task: Implement PocketBase Sync Engine (Android) - SPECIFICA DEFINITIVA

## 1. UI & Entry Point
- [x] Aggiungere l'opzione **"Apri file da PocketBase"** nel menu iniziale.
- [x] Implementare il flag globale `isCloudSyncEnabled()` (es. in `AppSettings` o `SyncManager`) che viene impostato a `true` solo se il DB è stato creato/aperto tramite il flusso PocketBase.

## 2. Inizializzazione Database (Cloud Mode)
- [x] Se selezionato "Apri da PocketBase", eseguire `table_V1_completo.sql` (nuovo schema con campi `pb_` e Trigger).
- [x] Avviare Wizard: URL, Email, Password.
- [x] **Pull Iniziale**: Scaricare tutte le collection (ordine definito in `table_config.js`) prima di rendere operativo il DB.

## 3. Architettura "Legacy Safe" (Entity & Repository)

### A. Modifiche a `EntityBase.java`
- [x] Aggiungere costanti per le colonne tecniche: `PB_ID`, `PB_UPDATED_AT`, `PB_IS_DIRTY`.
- [x] Implementare i metodi getter/setter per i campi `pb_`:
    - `getPbId() / setPbId(String id)`
    - `getPbUpdatedAt() / setPbUpdatedAt(String timestamp)`
    - `getPbIsDirty() / setPbIsDirty(int status)`
- [x] **Logica Condizionale**: I setter devono scrivere nei `ContentValues` solo se `isCloudSyncEnabled()` è vero. I getter devono restituire `null` (o `0` per isDirty) se le colonne non sono presenti.

### B. Modifiche ai Repository (es. `AccountRepository.java`)
- [x] Sovrascrivere `getAllColumns()` in ogni repository usando l'helper `addPbColumnsIfNeeded`.
- [x] Centralizzare l'aggiunta delle colonne in `RepositoryBase`.

## 4. Sync Manager (Logic)

### A. Realtime Pull (Sottoscrizioni)
- [ ] Inizializzare l'SDK PocketBase.
- [ ] Per ogni tabella in `SYNC_ORDER`, sottoscriversi agli eventi (`subscribe('*')`).
- [ ] Al ricevimento di un evento:
    - Aggiornare il record locale usando il `pb_id` come chiave di ricerca.
    - Impostare `pb_is_dirty = 2` durante l'operazione per bloccare i trigger locali di "auto-dirty".

### B. Push (Client -> Server)
- [x] Implementare una voce di menu **"Sincronizza ora"** (riutilizzando `R.id.menu_sync` esistente).
- [x] Logica di Push:
    1. Leggere ed eliminare su PB i record presenti in `pb_DELETED_RECORDS_LOG`.
    2. Scansionare le tabelle per record con `pb_is_dirty = 1`.
    3. Inviare il payload JSON limitato ai campi specificati in `table_config.js`.

## 5. Error Handling & Security
- [x] Implementare `AuthInterceptor` per gestire il token JWT.
- [x] Gestire i conflitti di sincronizzazione basandosi sul campo `pb_updated_at` (vince l'ultimo aggiornamento).
- [x] Assicurarsi che tutte le operazioni massive di pull siano racchiuse in una transazione SQLite per performance e integrità.


# Issue
- [X] create db on cloud
- [ ] pull from cloud: fail

---

### Note:
* **Mappatura**: Consulta `table_config.json` (risorsa raw) per la mappatura dinamica.
* **Agnosticismo**: Il codice mantiene la compatibilità con i database locali standard.
