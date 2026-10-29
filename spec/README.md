# Governance Spec-Driven - MMEX Android

Questo repository gestisce lo sviluppo e il refactoring di **Money Manager Ex per Android (MMEX)** tramite il protocollo **Spec-Driven Development (SDD)**.

> **ATTENZIONE CRITICA ALL'ARCHITETTURA REALE**:
> MMEX Android **NON** è una tipica app moderna (non usa Room, non usa Kotlin Coroutines/Flow per il core dati, non usa Hilt). 
> Il progetto è una codebase complessa basata su **Java**, **Dagger IoC classico**, **EventBus**, architettura custom multi-layer (`datalayer`/`domainmodel`), **Raw SQLite** (`res/raw/*.sql`), `MmxContentProvider` e motori di sincronizzazione custom (SyncAdapter classico e PocketBase).
> Qualsiasi modifica deve rispettare rigorosamente questo stack tecnologico.

## 1. Ciclo di Vita delle Modifiche
1. **Analisi Baseline**: Consulta i file in `spec/baseline/` (da `00` a `11`) che documentano in modo granulare l'architettura Java reale (classi, package, SQL, Eventi).
2. **Redazione CR (Change Request)**: Crea un file in `spec/cr/` derivato dal `CR-TEMPLATE.md`. Analizza minuziosamente l'impatto sul DB (Raw SQL), sui Repository, su EventBus e sui meccanismi di Sync.
3. **Approvazione & Implementazione**: Scrivi codice solo basandoti sui task atomici definiti e approvati nella CR, assicurandoti di implementare test adeguati (JUnit/Robolectric).
4. **Aggiornamento Baseline**: Una volta implementata la modifica, le sezioni rilevanti in `spec/baseline/` devono essere aggiornate per riflettere le nuove classi o tabelle.

## 2. Direttive per l'Assistente AI
- Non presumere stack tecnologici non documentati (niente Kotlin Flow se non esplicitamente richiesto per nuovi componenti isolati; rispetta l'uso di `EventBus` e `MmxCursorLoader`).
- Quando definisci un task o scrivi codice, cita sempre le classi Java e i package reali (es. `com.money.manager.ex.core.database.DatabaseManager`).
- Rispetta il protocollo di risoluzione conflitti (`DataMerger.java`) in caso di modifiche ai modelli di dominio marcati come `IModificationTraceable`.
