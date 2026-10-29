# Baseline 08: Feature - Scheduled and Workers

Questo documento descrive il sistema di gestione delle transazioni periodiche (Recurring/Scheduled) e l'intercettazione degli SMS bancari.

## 1. Architettura delle Transazioni Periodiche (`scheduled/`)
In MMEX, le transazioni periodiche ("Bills and Deposits") non entrano nel registro del conto finché non scade la loro data. Il sistema deve anticiparne l'esecuzione per renderle "Reali".

- **`Recurrence` / `RecurringTransactionProcess`**: Motore logico che, dato un oggetto `RecurringTransaction`, calcola le date future (Next Occurrence) sulla base di un cron-like setting (Es. "Ogni 2 Mesi").
- **`ScheduledTransactionForecastList`**: Fragment che aggrega le future uscite programmate, aiutando l'utente a stimare il flusso di cassa futuro.
- **`ScheduledTransactionEditActivity`**: Activity dedicata alla modifica del template di una ricorrenza (che contiene importo base, payee e frequenza), ma non incide direttamente sul saldo finché non viene srotolata (Unrolled).

## 2. Worker di Processamento (`scheduled/worker/`)
L'app utilizza i servizi di background di Android per svegliarsi e controllare se una spesa ricorrente deve essere inserita nel registro effettivo.
- **`RecurringTransactionBootReceiver`**: `BroadcastReceiver` in ascolto di `BOOT_COMPLETED` per ri-registrare gli allarmi dopo un riavvio del device.
- **`ScheduledTransactionWorker`** (WorkManager): Periodicamente interroga il database per transazioni con `NextOccurrenceDate <= Oggi`. Per ognuna, invoca il `RecurringTransactionService` che duplica il template in una `AccountTransaction` vera e propria nel conto, aggiornando la prossima data utile del template.

## 3. Analisi Automatica (SMS/Notifiche - `notifications/`)
- **`SmsReceiverTransactions`**: `BroadcastReceiver` storico in ascolto degli SMS in entrata. Cerca di fare parsing del testo (tramite regex definite nelle impostazioni utente) per riconoscere importo, payee e conto (in base alle ultime 4 cifre del CC). Se riconosce una transazione bancaria, genera una `AccountTransaction` in stato "Draft" o notifica l'utente per approvarla.
