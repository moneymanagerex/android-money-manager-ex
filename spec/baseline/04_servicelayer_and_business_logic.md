# Baseline 04: Service Layer and Business Logic

Questo documento descrive il livello di astrazione dedicato alla logica di business e all'esportazione dei dati, situato sopra i Repository (`datalayer/`).

## 1. Architettura Service Layer (`servicelayer/`)
Il pattern di progettazione prevede che frammenti e activity non chiamino quasi mai direttamente i repository, ma passino per i "Service" che incapsulano le regole applicative (es. aggiornamento saldi a cascata, validazioni, controlli di integrità relazionale).

- **`ServiceBase`**: Classe base per i servizi. Solitamente contiene riferimenti al contesto e risolve le dipendenze tramite Dagger (iniettando i repository necessari).
- **Iniezione**: I servizi vengono tipicamente istanziati e iniettati nei ViewModel o nei Fragment tramite Dagger `MmxComponent`.

## 2. Servizi Core di Business
- **`AccountService`**: Regole relative ai conti (calcolo saldi inziali, chiusura conto, validazione valute associate).
- **`CategoryService`**: Logica di gestione dell'albero delle categorie, controllo orfani o categorie in uso.
- **`InfoService`**: Gestione dei metadati del database (versione schema, configurazioni salvate su DB).
- **`PayeeService`**: Logica per il partizionamento e la normalizzazione dei beneficiari.
- **`RecurringTransactionService`**: Motore applicativo per lo srotolamento delle transazioni ricorrenti in transazioni effettive nel registro (bills/deposits).
- **`TagService`**: Validazione, associazione multipla di Tag alle transazioni tramite la tabella ponte `TAGLINK_V1`.

## 3. Motore di Esportazione e Importazione
La logica di scambio dati verso formati standard è isolata dal DB locale.

### 3.1 Esportazione QIF (`servicelayer/qif/`)
Il formato Quicken Interchange Format è gestito tramite un framework custom:
- **`QifExport`**: La classe orchestratrice che recupera i conti e delega la generazione.
- **`QifGenerator`**: Motore che itera sui cursori delle transazioni (utilizzando `MmxCursorLoader` o le query del repository) e mappa i dati testuali.
- **`QifHeader`**: Modella l'intestazione standard di una transazione QIF (es. `!Type:Bank`).
- **`QifRecord`**: Modella i singoli campi di un record (data, importo, categoria, beneficiario).

### 3.2 Esportazione CSV
- **`core/ExportToCsvFile.java`**: Utility legacy robusta per l'esportazione tabellare dei report o del registro transazioni, gestendo in modo accurato gli escape dei caratteri separatori.
