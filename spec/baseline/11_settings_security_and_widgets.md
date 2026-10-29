# Baseline 11: Settings, Security and Widgets

Configurazioni, sicurezza biometrica e integrazioni sulla HomeScreen Android.

## 1. Impostazioni Applicazione (`settings/`)
Le configurazioni sono organizzate in `SharedPreferences` gerarchiche e divise logicamente.
- **Gerarchia Classi**: `AppSettings`, `SettingsBase`.
- **Fragment di Configurazione**: Mappano i file `.xml` presenti in `res/xml/`.
  - `preferences_general.xml`: Impostazioni globali (tema, lingua).
  - `preferences_sync.xml` e `SyncPreferences`: Parametri di sincronizzazione (pocketbase url, google drive tokens, auto-sync abilitato).
- **Impostazioni Per-Database**: 
  - `DatabaseSettings` e `PerDatabaseFragment`: Alcune preferenze non sono globali dell'app, ma risiedono (o sono legate) all'attuale database `.mmb` aperto (es. default account, valuta base). Spesso i dati vengono scritti e letti nella tabella `INFOTABLE_V1`.

## 2. Sicurezza e Sblocco (`passcode/`)
Livello di protezione UI all'apertura dell'applicazione (indipendente dalla crittografia SQLCipher).
- **`PasscodeActivity`**: Schermata di blocco con PIN (Pattern o Tastierino numerico) richiesta in fase di `onResume` dell'App.
- **`FingerprintHandler`**: Integrazione (spesso tramite AndroidX Biometric) che bypassa la digitazione del PIN se l'utente si autentica con impronta digitale/FaceID. L'handler incapsula i callback crittografici.

## 3. Android Home Widgets (`widget/`)
L'app estende le funzionalità core al launcher di Android per accesso rapido (senza aprire l'app).
Tutti i widget estendono `AppWidgetProvider`.

- **`AccountBillsWidgetProvider`**: Mostra nella home screen un listato sintetico delle prossime bollette/spese pianificate in scadenza.
- **`ButtonAddTransactionWidgetProvider`**: Fornisce un widget piccolissimo (tipo 1x1) che è semplicemente un bottone (Shortcut) per lanciare direttamente `CheckingTransactionEditActivity` senza passare dalla Home.
- **`SingleAccountWidget`**: Legato univocamente a un `AccountID`, fa apparire a schermo il saldo aggiornato di quel conto corrente.
- **`SummaryWidgetProvider`**: Un widget generico (es. 4x2) che interroga il DB in background, formatta il Net Worth e il Cashflow del mese corrente e li mostra sulla home del device. Per il refresh dipendono fortemente dagli intent (BroadcastReceiver) emessi da `DatabaseManager` in caso di mutazioni dati.
