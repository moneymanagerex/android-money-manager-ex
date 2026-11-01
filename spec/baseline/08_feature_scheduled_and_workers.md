# Baseline 08: Feature - Scheduled and Workers

This document describes the management system for periodic transactions (Recurring/Scheduled) and the interception of banking SMS.

## 1. Periodic Transactions Architecture (`scheduled/`)
In MMEX, periodic transactions ("Bills and Deposits") do not enter the account ledger until their date expires. The system must anticipate their execution to make them "Real".

- **`Recurrence` / `RecurringTransactionProcess`**: Logical engine that, given a `RecurringTransaction` object, calculates future dates (Next Occurrence) based on a cron-like setting (e.g., "Every 2 Months").
- **`ScheduledTransactionForecastList`**: Fragment that aggregates future scheduled expenses, helping the user estimate future cash flow.
- **`ScheduledTransactionEditActivity`**: Dedicated activity to edit a recurrence template (containing base amount, payee, and frequency), but does not directly affect the balance until unrolled.

## 2. Processing Worker (`scheduled/worker/`)
The app uses Android background services to wake up and check if a recurring expense needs to be inserted into the actual ledger.
- **`RecurringTransactionBootReceiver`**: `BroadcastReceiver` listening for `BOOT_COMPLETED` to re-register alarms after a device reboot.
- **`ScheduledTransactionWorker`** (WorkManager): Periodically queries the database for transactions with `NextOccurrenceDate <= Today`. For each, it invokes the `RecurringTransactionService`, which duplicates the template into an actual `AccountTransaction` in the account, updating the template's next valid date.

## 3. Automatic Analysis (SMS/Notifications - `notifications/`)
- **`SmsReceiverTransactions`**: Historical `BroadcastReceiver` listening to incoming SMS. Attempts to parse the text (via regex defined in user settings) to recognize amount, payee, and account (based on the last 4 digits of the CC). If it recognizes a banking transaction, it generates an `AccountTransaction` in "Draft" status or notifies the user to approve it.