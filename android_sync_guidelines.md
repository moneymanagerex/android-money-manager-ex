# Specifiche di Sincronizzazione per l'Ambiente Android (AMMEX Sync)

Questo documento fornisce le specifiche tecniche dettagliate per implementare la sincronizzazione "Offline-First" con PocketBase nell'applicazione Android (AMMEX), garantendo la coerenza dei dati con il client Desktop.

---

## 1. Logica dei Tre Stati (`pb_is_dirty`)
Per evitare loop infiniti di sincronizzazione e tracciare le modifiche offline, ogni tabella sincronizzata nel database locale SQLite deve includere tre colonne tecniche:
1.  **`pb_id` (TEXT):** L'identificativo univoco del record remoto su PocketBase.
2.  **`pb_is_dirty` (INTEGER):** Lo stato del record locale:
    *   **`0` (Synced):** Il record locale è allineato con il server.
    *   **`1` (Local Change):** Il record è stato modificato/inserito offline e va inviato al server.
    *   **`2` (Cloud Ingress):** Disattiva temporaneamente i trigger per impedire loop durante la scrittura da rete.
3.  **`pb_updated_at` (TEXT):** Data/ora UTC dell'ultima modifica (formato ISO-8601).

### Trigger SQLite Locali su Android
Ogni tabella sincronizzata deve disporre di trigger locali:
*   **INSERT:** Se `pb_is_dirty != 2`, imposta automaticamente `pb_is_dirty = 1` e valorizza `pb_updated_at`.
*   **UPDATE:** Se `pb_is_dirty != 2` e viene modificato un campo non tecnico, imposta `pb_is_dirty = 1` e aggiorna `pb_updated_at`.
*   **DELETE:** Prima dell'eliminazione fisica di un record con `pb_id` non nullo, inserisce il nome della tabella e il `pb_id` nella tabella tecnica `pb_DELETED_RECORDS_LOG`.

---

## 2. Ordinamento delle Tabelle (`SYNC_ORDER`)
Per prevenire errori di integrità referenziale (Foreign Key constraints), le tabelle devono essere sincronizzate nell'ordine specificato in [table_config.js](file:///c:/Users/EmmanuelePrudenzano/StudioProjects/MMEX-Sync/mmex-sync/src/config/table_config.js):

1.  **Livello 1 (Indipendenti):** `INFOTABLE_V1`, `CURRENCYFORMATS_V1`, `TAG_V1`, `BUDGETYEAR_V1`, `CUSTOMFIELD_V1`.
2.  **Livello 2 (Anagrafiche):** `CATEGORY_V1` (attenzione alla gerarchia `PARENTID`), `PAYEE_V1`, `ACCOUNTLIST_V1`.
3.  **Livello 3 (Entità e Movimenti):** `ASSETS_V1`, `STOCK_V1`, `CHECKINGACCOUNT_V1`, `BILLSDEPOSITS_V1`, `BUDGETTABLE_V1`.
4.  **Livello 4 (Dettagli e Collegamenti):** `SPLITTRANSACTIONS_V1`, `BUDGETSPLITTRANSACTIONS_V1`, `CURRENCYHISTORY_V1`, `STOCKHISTORY_V1`, `ATTACHMENT_V1`, `TAGLINK_V1`, `TRANSLINK_V1`, `SHAREINFO_V1`, `CUSTOMFIELDDATA_V1`.

---

## 3. Protocollo di Gestione del Flusso di PUSH (Locale $\rightarrow$ Server)

La fase di Push invia al server PocketBase tutte le modifiche locali accumulate offline. Per ciascuna tabella (nell'ordine definito al punto 2), estrarre tutti i record aventi `pb_is_dirty = 1` o privi di `pb_id`, ed elaborare ogni riga secondo la logica definita di seguito:

### Scenario A: `pb_id` Vuoto (Record nato in locale)
1.  **Invio:** Effettuare una richiesta di `CREATE` sul server PocketBase.
2.  **Esito Positivo (OK):** Salvare il `pb_id` restituito dal server nel record locale e impostare `pb_is_dirty = 0`.
3.  **Esito Negativo (KO):** Se la creazione fallisce a causa di una violazione della chiave primaria (PK) o di un vincolo di unicità (Unique):
    *   Se l'errore restituito dal server è `validation_not_unique`, estrarre l'elenco dei campi che sono oggetto della violazione di unicità (es. `REFTYPE`, `REFID`, `TAGID` per `TAGLINK_V1`). Altrimenti, fare riferimento alla chiave primaria (PK) del record.
    *   Eseguire una query di ricerca sul server PocketBase per trovare un record remoto con la stessa PK o con lo stesso valore per i campi Unique estratti.
    *   **Se il record remoto viene trovato:** Risolvere il conflitto sostituendo il record locale con quello remoto. Eliminare fisicamente il record locale con la chiave originaria ed inserire il record remoto compilando correttamente il `pb_id`, le colonne collegate e impostando `pb_is_dirty = 0`.

### Scenario B: `pb_id` Non Vuoto (Record già sincronizzato in precedenza)
1.  **Invio:** Effettuare una richiesta di `UPDATE` sul server PocketBase per il corrispondente `pb_id`.
2.  **Esito Positivo (OK):** Impostare `pb_is_dirty = 0` sul database locale.
3.  **Esito Negativo (KO):** Se l'aggiornamento fallisce, significa che il record è stato cancellato dal server (errore 404).
    *   **Risoluzione:** Provare a ricreare il record sul server PocketBase tramite una chiamata `CREATE` passando esplicitamente il `pb_id` locale originario (che non è più presente sul server).
    *   **Se la `CREATE` ha successo:** Impostare `pb_is_dirty = 0` sul record locale.
    *   **Se la `CREATE` fallisce:** Significa che l'operazione urta contro un vincolo di unicità. Utilizzare lo stesso meccanismo descritto nello *Scenario A* (KO) per identificare la PK o i campi Unique, cercare il record sul server ed effettuare la sostituzione locale del record con quello remoto.

### Gestione Conflitti Temporali (Errore 409)
In qualsiasi chiamata di `UPDATE` o `CREATE` (nel caso in cui il server gestisca il controllo di versione delle righe), il server potrebbe restituire un errore **409 Conflict**, che indica che la versione del record sul server è più recente di quella locale.
*   **Risoluzione:** Ignorare le modifiche locali sul record corrente, scaricare il record aggiornato dal server (es. tramite `getById`), aggiornare il database locale con i dati remoti e impostare lo stato del record a `0` (sincronizzato).

---

## 4. Gestione del Flusso di PULL (Server $\rightarrow$ Locale)
1.  **Filtro Incrementale (lastSync):** Richiedere solo i record modificati dopo l'ultimo sync completato (`_updated_at > lastSync`), sottraendo una finestra di tolleranza di 5 secondi (`lastSync - 5s`).
2.  **Sincronizzazione Forzata (Force Mode):** Ignorare il timestamp e scaricare tutti i dati.
3.  **Applicazione delle modifiche:**
    *   **Se il record remoto NON esiste localmente:** Se `_is_deleted = 0`, inserire nel DB locale impostando temporaneamente `pb_is_dirty = 2`, inserire i dati e poi aggiornare a `0`.
    *   **Se il record remoto ESISTE già localmente:**
        *   Se `_is_deleted != 0` sul server, eliminare fisicamente il record locale.
        *   Altrimenti, aggiornare tutti i campi locali con i valori del server impostando `pb_is_dirty = 2` durante la scrittura e infine impostarlo a `0`.

---

## 5. Gestione delle Cancellazioni Locali (DELETE $\rightarrow$ Server)
1.  Durante la sincronizzazione, estrarre tutte le righe dalla tabella tecnica locale `pb_DELETED_RECORDS_LOG`.
2.  Per ciascuna riga, inviare una richiesta `DELETE` al server PocketBase utilizzando il `PB_ID` registrato.
3.  Se la richiesta restituisce **404 (Not Found)**, ignorare l'errore (il record era già stato rimosso).
4.  Completato il ciclo per tutte le righe, svuotare interamente la tabella `pb_DELETED_RECORDS_LOG`.

---

## 6. Specifiche ed Attenzioni per lo Sviluppo in Android (Kotlin/Java)
*   **Transazioni:** Racchiudere la scrittura locale durante la Pull all'interno di transazioni SQLite per preservare consistenza e performance.
*   **Inizializzazione Trigger in Room/SQLite:** Assicurarsi che alla migrazione dello schema o all'apertura del DB i trigger tecnici vengano ricreati correttamente.
*   **Thread Safety e Connessione:** Proteggere la sincronizzazione da chiamate concorrenti sovrapposte (es. utilizzando un mutex o un flag di stato a livello di servizio).
*   **Recovery dopo arresto anomalo:** In caso di crash o disconnessione durante la sincronizzazione, all'avvio successivo reimpostare i record rimasti in stato `2` allo stato `1`.
