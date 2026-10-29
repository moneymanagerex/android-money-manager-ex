# Baseline 00: Project and Build Variants

## 1. Struttura dei Source Sets (`app/src/`)
Il modulo principale `:app` gestisce diverse build variants e flavor, orchestrate tramite Gradle. L'albero dei sorgenti riflette una profonda separazione tra i target di distribuzione e le suite di test.

### 1.1 Varianti di Distribuzione (Flavors / Build Types)
- **`app/src/main/`**: Il core dell'applicazione MMEX contenente la maggior parte del codice Java, delle risorse UI e della configurazione del Manifest principale.
- **`app/src/fdroid/`**: Variante specifica per il rilascio su F-Droid, senza dipendenze proprietarie di Google Play Services (es. rimozione di tracciamenti proprietari o sync legato a Google Drive).
- **`app/src/beta/`**: non viene più usata.

### 1.2 Varianti di Test
- **`app/src/test/`**: Unit test eseguiti sulla JVM locale (JUnit 4). Focus sui calcoli di business e logiche disaccoppiate da Android.
- **`app/src/androidTest/`**: Test strumentali e test basati su **Robolectric**. Include configurazioni dedicate nel package `org.moneymanagerex.android.tests`.
  - **`TestApplication`**: Sottoclasse dell'applicazione usata durante i test per iniettare mock o un database in-memory saltando l'inizializzazione standard di Dagger (MmxComponent).

**Nota**: tutta la struttura di test è stata aggioranta in modo parziale e non rispecchia tutte le funzionalità.

---

## 2. Gestione degli Assets e SQL Raw (`app/src/main/`)

A differenza delle app che usano ORM, MMEX utilizza una gestione manuale e fortemente strutturata per asset e query SQL.

### 2.1 Assets (`src/main/assets/`)
- **`fonts/`**: Contiene tipografia custom e l'icon-font di MMEX (condiviso/collegato con `:MMEX_Icon_Font`).
- **`tutorial_images/`**: Immagini per il carosello di on-boarding iniziale o la guida introduttiva.

### 2.2 Raw SQL (`src/main/res/raw/`)
Questa directory è il cuore dell'interazione col database.
- **File di Setup Schema**: Contiene `tables_v1.sql` (lo schema iniziale completo).
- **File di Migrazione**: Serie di file `database_version_0.sql` fino a `database_version_21.sql` (o successivi) usati da `MmxOpenHelper` per i delta di aggiornamento.
- **File di Query (`query_*.sql`)**: Query complesse multi-tabella caricate a runtime per la reportistica e la dashboard (es. bilancio incrociato).
- **`table_config.json`**: File descrittore che mappa la struttura delle tabelle e dei metadati associati.
