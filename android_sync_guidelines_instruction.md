# Linee Guida per l'Implementazione della Sincronizzazione nell'App Android (AMMEX Sync)

Questo documento descrive i requisiti tecnici, i casi d'uso e le regole di business che l'applicazione Android (AMMEX) deve implementare nel proprio database e nei servizi di sincronizzazione per integrarsi correttamente con il backend PocketBase e mantenere la piena coerenza dei dati con la versione Desktop.

---

## 1. Struttura del Database e Protocollo a Tre Stati

Per evitare loop infiniti di sincronizzazione e tracciare correttamente lo stato dei dati in modalità "offline-first", ogni tabella sincronizzata nel database locale SQLite del dispositivo Android deve contenere tre colonne tecniche aggiuntive:
1.  **`pb_id` (TEXT):** L'identificativo univoco del record sul server PocketBase (nullo o vuoto per record creati offline e non ancora sincronizzati).
2.  **`pb_is_dirty` (INTEGER):** Indica lo stato di sincronizzazione locale del record:
    *   **`0` (Synced):** Il record locale è allineato con il server.
    *   **`1` (Local Change):** Il record è stato inserito o modificato localmente e deve essere inviato al server.
    *   **`2` (Cloud Ingress):** Stato temporaneo. Indica che il record viene aggiornato/inserito a seguito di un'operazione di sincronizzazione remota (Pull). Questo stato disattiva i trigger locali per prevenire loop infiniti.
3.  **`pb_updated_at` (TEXT):** Timestamp dell'ultimo aggiornamento (in formato ISO-8601 UTC).

### Trigger SQLite Locali su Android
L'app Android deve definire per ciascuna tabella dei trigger SQLite equivalenti a quelli desktop:
*   **Trigger di INSERT:** Quando un record viene inserito con `pb_is_dirty != 2`, imposta automaticamente `pb_is_dirty = 1` e valorizza `pb_updated_at` con il tempo corrente.
*   **Trigger di UPDATE:** Quando una colonna non tecnica viene modificata e `pb_is_dirty != 2`, imposta automaticamente `pb_is_dirty = 1` e aggiorna `pb_updated_at`.
*   **Trigger di DELETE:** Prima di eliminare un record avente un `pb_id` non nullo, inserisce una riga contenente il nome della tabella e il `pb_id` nella tabella di log tecnica `pb_DELETED_RECORDS_LOG`.

---

## 2. Ordinamento della Sincronizzazione (`SYNC_ORDER`)

Per evitare violazioni dei vincoli di chiave esterna (Foreign Key) sia nel database locale SQLite che sulle collezioni di PocketBase, le tabelle devono essere elaborate rigorosamente in sequenza (in ordine crescente per il **Push** e il **Pull**, assicurandosi che le tabelle dipendenti siano aggiornate solo dopo i loro padri):

```markdown
1.  INFOTABLE_V1
2.  CURRENCYFORMATS_V1
3.  TAG_V1
4.  BUDGETYEAR_V1
5.  CUSTOMFIELD_V1
6.  CATEGORY_V1 (Autoreferenziale: gestire con cura la gerarchia PARENTID)
7.  PAYEE_V1
8.  ACCOUNTLIST_V1
9.  ASSETS_V1
10. STOCK_V1
11. CHECKINGACCOUNT_V1
12. BILLSDEPOSITS_V1
13. BUDGETTABLE_V1
14. SPLITTRANSACTIONS_V1
15. BUDGETSPLITTRANSACTIONS_V1
16. CURRENCYHISTORY_V1
17. STOCKHISTORY_V1
18. ATTACHMENT_V1
19. TAGLINK_V1
20. TRANSLINK_V1
21. SHAREINFO_V1
22. CUSTOMFIELDDATA_V1
```

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

Il pull recupera le modifiche effettuate su altri dispositivi ed allinea il DB locale.
1.  **Filtro Incrementale (lastSync):** Per ottimizzare le performance, richiedere al server solo i record modificati dopo il timestamp dell'ultima sincronizzazione riuscita (`_updated_at > lastSync`). Sottoscrivere una tolleranza di sicurezza di circa 5 secondi (`lastSync - 5s`) per evitare di perdere scritture concorrenti.
2.  **Sincronizzazione Forzata (Force Mode):** Se richiesta esplicitamente (o al primo avvio), ignorare il filtro temporale e scaricare l'intera lista dei record.
3.  **Applicazione dei Record Ricevuti:**
    *   **Se il record remoto non esiste localmente:**
        *   Se è marcato sul server come eliminato (`_is_deleted != 0`), ignorarlo.
        *   Altrimenti, inserire il record locale impostando temporaneamente `pb_is_dirty = 2` (per disattivare i trigger locali), inserire i dati e poi impostare `pb_is_dirty = 0`.
    *   **Se il record remoto esiste già localmente (stesso `pb_id`):**
        *   Se sul server è marcato come eliminato (`_is_deleted != 0`), procedere con l'eliminazione fisica del record dal database locale.
        *   Altrimenti, aggiornare tutti i campi locali con i valori remoti impostando `pb_is_dirty = 2` durante l'aggiornamento e resettandolo a `0` a transazione ultimata.

---

## 5. Gestione delle Cancellazioni Locali (DELETE $\rightarrow$ Server)

Quando l'utente elimina dei dati dall'interfaccia Android offline:
1.  L'eliminazione locale valorizza la tabella `pb_DELETED_RECORDS_LOG` tramite il trigger `TRG_DELETE`.
2.  Durante la fase di sincronizzazione, inviare una richiesta `DELETE` al server per ciascun `PB_ID` presente nel log delle cancellazioni.
3.  **Gestione del 404 su Delete:** Se il server restituisce 404 (il record è già stato cancellato sul server da un altro dispositivo), l'errore va ignorato.
4.  Svuotare interamente la tabella `pb_DELETED_RECORDS_LOG` locale al completamento positivo dell'operazione.

---

## 6. Specifiche ed Attenzioni per lo Sviluppo in Android (Kotlin/Java)

*   **Gestione delle transazioni SQLite:** Tutte le operazioni di scrittura locale durante l'applicazione del Pull o del Push devono essere racchiuse in transazioni SQLite (`db.beginTransaction()` / `db.endTransaction()`) per garantire atomicità e performance elevate.
*   **Gestione dei Trigger in Room / SQLite Open Helper:** Se si utilizza la libreria Room di Jetpack, prestare attenzione al fatto che le modifiche allo schema (aggiunta di colonne tecniche e trigger) devono essere gestite tramite Migrations esplicite. I trigger devono essere ricreati esplicitamente all'apertura del database (`RoomDatabase.Callback.onOpen`).
*   **Disattivazione dei Trigger (Stato 2):** Assicurarsi che le funzioni che scrivono nel DB locale durante il pull utilizzino rigorosamente la logica dello stato `pb_is_dirty = 2` prima di procedere con l'operazione di inserimento/aggiornamento, altrimenti si verificheranno crash di violazione di vincoli o cicli infiniti di aggiornamento dei timestamp locali.
*   **Ripristino in caso di fallimento:** Se una sincronizzazione viene interrotta (es. perdita improvvisa di connettività), implementare un blocco `try-finally` o un meccanismo di recovery all'avvio che reimposti tutti i record locali rimasti bloccati in stato `2` allo stato `1`.
*   **Gestione del Fuso Orario:** Tutti i timestamp scambiati con PocketBase devono essere memorizzati in formato UTC ISO-8601 (es. `yyyy-MM-dd'T'HH:mm:ss'Z'`).
