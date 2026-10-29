# Baseline 07: Feature - Accounts and Transactions

Questo documento descrive il flusso delle feature principali di MMEX, mappando l'ingresso dell'utente dall'app fino all'inserimento di una transazione e al ricalcolo asincrono dei bilanci.

## 1. App Entry e Dashboard (`home/`, `search/`)
- **`MainActivity`**: L'host contenitore principale con il Navigation Drawer.
- **`HomeFragment` / `DashboardFragment`**: Punti di atterraggio. Riepilogano il patrimonio netto aggregando i conti correnti (Asset) al netto delle passività.
- **Ricerca (`search/`)**:
  - **`SearchParameters`**: Modello che raccoglie filtri utente (importo, data, tag, nota, stato, payee). Viene passato al `Dataset` che genera dinamicamente il `WhereStatementGenerator`.

## 2. Flusso Conti Correnti (`account/`)
- La lista viene servita da `AbsRecyclerFragment` agganciato a `AccountRepository`.
- **Ricalcolo dei Saldi (`CalculateRunningBalanceTask2`)**: Un task asincrono cruciale (storicamente `AsyncTask` o worker) che ricomputa il saldo corrente di un conto iterando su tutto il suo registro transazioni dal "Saldo Iniziale". Non si basa su trigger SQL ma su logica applicativa Java, al fine di ricalcolare i bilanci progressivi riga per riga (Running Balance).

## 3. Flusso Transazioni (`transactions/`)
- **Modifica ed Inserimento**: 
  - **`CheckingTransactionEditActivity`**: La complessa Activity di edit di una transazione (incassa Input Dialog per l'importo, Spinner per categorie/payee, DatePicker).
  - **`EditTransactionCommonFunctions`**: Classe helper che astrae validazioni e logiche condivise (es. validazione importo non nullo o gestione cambi valuta per trasferimenti internazionali).
- **Gestione Categorie Annidate (`nestedcategory/`)**: 
  - MMEX non ha tabelle separate per le sub-categorie, usa il ParentID. `NestedCategoryEntity` e `QueryNestedCategory` ricompletano l'albero (Parent > Child) mostrandolo nello spinner della UI.
- **Flusso Tag (`tag/`)**:
  - Implementazione M2M (Many-To-Many). Un'interfaccia (`TagLinkUtils`) permette di visualizzare un campo di testo con Tag (separati da virgole), che vengono parsati e tradotti in query sulla tabella di join `TAGLINK_V1` durante il salvataggio della transazione.

## 4. Transazioni Suddivise (Split - `SplitCategoriesActivity`)
Una singola spesa al supermercato può essere categorizzata su multipli rami (Cibo, Detersivi).
- Viene mappata tramite la tabella `SPLITTRANSACTIONS_V1`.
- L'activity `SplitCategoriesActivity` gestisce un adapter speciale dove l'utente aggiunge N righe. La somma delle righe Split deve combaciare con l'importo totale della `AccountTransaction` padre, altrimenti viene mostrato un avviso di sbilanciamento e il salvataggio è bloccato.
