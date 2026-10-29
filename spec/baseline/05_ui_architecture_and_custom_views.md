# Baseline 05: UI Architecture and Custom Views

Questo documento illustra l'infrastruttura dell'interfaccia utente (UI), che combina pattern storici Android con layout modernizzati.

## 1. Gerarchia delle Activity e dei Fragment
Tutta la logica di presentazione eredita da robuste classi base che garantiscono coerenza (es. gestione automatica dei Loader, iniezione Dagger e temi).

- **`MmxBaseFragmentActivity`**: Activity principale (Appcompat-based). Registra EventBus in `onStart`/`onStop`, prepara il `MmxComponent` (Dagger) e gestisce la toolbar.
- **Fragment di Lista**:
  - **`AbsListFragment` / `BaseListFragment`**: Fragment legacy basati su `ListView`. Gestiscono i cursori e mostrano layout di "Empty State".
  - **`AbsRecyclerFragment` / `BaseRecyclerFragment`**: Evoluzione moderna basata su `RecyclerView`. Standard per le schermate a lista (Conti, Categorie, Transazioni).
- **`AllDataListFragment`**: Base astratta per mostrare set di dati complessi incrociati (`QueryAllData`), implementando logiche generiche di raggruppamento e ordinamento.

## 2. Architettura Cursori e Adapters
MMEX usa estensivamente i `Loader` (LoaderManager storico) per interrogare il database in background e aggiornare l'UI reattivamente quando il ContentProvider notifica un cambiamento.

- **`MmxCursorLoader`**: Loader custom che interroga `MmxContentProvider`. Quando un record viene modificato altrove, il cursore si invalida, forzando un refresh della lista.
- **`MoneySimpleCursorAdapter`**: Adapter bridge usato nelle combo box (Spinner) e per liste legacy, mappando colonne SQLite a viste XML.
- **`AllDataAdapter`**: Adapter complesso per le RecyclerView, in grado di gestire ViewType multipli (intestazioni di data, transazioni, totalizzatori).
- **Pattern `*ViewHolder`**: Ogni entità ha un ViewHolder associato (es. `TransactionViewHolder`) che estrae i dati dal cursore nella posizione corrente ed effettua il binding sui widget.

## 3. Custom Views e Interazione Utente (`view/`, `common/`)
- **Gestione Font Custom (`Roboto*`)**: Classi come `RobotoTextView` o `RobotoButton` che ereditano dai widget base applicando dinamicamente il typeface corretto (o i font vettoriali mappati nel modulo `:MMEX_Icon_Font`).
- **`ContextMenuRecyclerView`**: Estensione di RecyclerView per supportare i menu contestuali classici di Android al long-press (modifica, elimina, duplica).
- **Dialogs e Input**:
  - **`AmountInputDialog`**: Dialog personalizzato ad alta affidabilità per l'inserimento di valori monetari (gestione virgola/punto, localizzazione).
  - **`CalculatorActivity`**: Activity popup che funge da calcolatrice integrata, ritornando il risultato alla vista chiamante (es. form di transazione).

*(Nota: L'introduzione del pacchetto `viewmodels/` in alcune sezioni affianca o sostituisce gradualmente i loader con `ViewModel` AndroidX, spesso appoggiati a LiveData o chiamate asincrone tramite EventBus.)*
