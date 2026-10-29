# Baseline 06: Sync and PocketBase Engine

La sincronizzazione in MMEX Android è una delle funzionalità più critiche e complesse, essenziale per mantenere l'allineamento con il client Desktop. Supporta due strategie architetturali distinte (classica e PocketBase).

## 1. Motore Sync Classico (SyncAdapter & Workers)
Inizialmente basato sul pattern SyncAdapter di Android e poi migrato/affiancato a WorkManager.

- **`SyncManager` / `SyncService`**: Punti di ingresso storici del sync (spesso collegati a Google Drive o Dropbox).
- **`SyncAdapter`**: Thread astratto di Android per sincronizzazioni periodiche aggregate.
- **WorkManager (`sync/jobmanager/`)**:
  - **`DownloadWorker` / `UploadWorker`**: Task in background che scaricano il database MMEX remoto (es. Cloud) e caricano le differenze locali.
  - **`SyncWorker`**: Job unificato che orchestra l'intero ciclo Push/Pull.

## 2. PocketBase Sync Engine (`sync/pocketbase/`)
Un motore di sincronizzazione moderno basato su API REST verso un backend **PocketBase**. A differenza del sync basato su interi file, PocketBase sincronizza **record individuali**.

- **`PocketBaseClient`**: Inizializza il client e gestisce il token JWT di autenticazione.
- **`PocketBaseApiService`**: Interfacce Retrofit (o HTTP client custom) per le chiamate CRUD alle collection di PocketBase.
- **`PocketBaseSyncEngine`**: Orchestratore che interroga l'endpoint `/api/collections/...` e applica il push/pull dei record in base all'hash e alla data.
- **`PocketBaseSetupActivity`**: UI per l'inserimento dell'URL del server, e-mail e password dell'utente.

## 3. Data Merger e Risoluzione dei Conflitti (`sync/merge/`)
Il cuore nevralgico per unire DB remoti (file o record) con il DB locale.

- **Timestamp & Traceability**: Utilizzo cruciale dell'interfaccia `IModificationTraceable`. I campi `UPDATEDTIME` determinano quale versione vince in caso di conflitto.
- **`DataMerger.java`**: Motore che itera tabella per tabella (Account, Payee, Transaction), confrontando i record. Supporta le operazioni di Insert, Update e Soft-Delete.
- **`MergeConflictResolution.java`**: Logica di risoluzione (LWW - Last Write Wins, base di default).
- **Eventi in `sync/events/`**: L'engine di sync notifica la UI sul progresso emettendo eventi sull'EventBus (es. `SyncProgressEvent`, `SyncCompletedEvent`), permettendo ai `MmxCursorLoader` delle liste di invalidarsi e ricaricare i dati aggiornati senza intervento utente.
