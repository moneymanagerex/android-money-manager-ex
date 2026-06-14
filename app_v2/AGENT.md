# CONTESTO PROGETTO: MMEX Android V2 Rewrite

## Obiettivo
Riscrittura completa dell'app Android legacy (Money Manager Ex) utilizzando principi di Clean Architecture e uno stack Android moderno. Il progetto funge da base solida per la gestione finanziaria locale e deve predisporre l'architettura per future integrazioni di sincronizzazione (es. con backend PocketBase per MMX Sync).

## Autore / Maintainer
@wolfsolver

## 1. VINCOLI DI ISOLAMENTO (Strangler Fig Pattern)
- **Nessuna modifica al codice legacy:** Tutto il nuovo codice, le risorse e le dipendenze devono essere rigorosamente confinate all'interno del modulo `:app_v2`.
- Il codice della vecchia app non deve mai essere rifattorizzato o importato direttamente nel nuovo modulo.

## 2. STACK TECNOLOGICO OBBLIGATORIO
- **Linguaggio:** Kotlin 100% (usare sintassi idiomatica, sealed classes, scope functions).
- **Interfaccia Utente:** Jetpack Compose (Material Design 3). Assoluto divieto di utilizzare layout XML, Fragment o RecyclerView.
- **Database:** Room Database con KSP.
- **Asincronia:** Kotlin Coroutines e Flow (StateFlow/SharedFlow). Divieto di RxJava o LiveData.
- **Dependency Injection:** Hilt.
- **Architettura:** Clean Architecture con pattern MVVM (Model-View-ViewModel) o MVI per il layer di presentazione.

## 3. REGOLE DEL LAYER DATI (Il Database Legacy)
- **Schema Intoccabile:** Lo schema del database SQLite originale preesistente è intoccabile per garantire la compatibilità con l'applicativo desktop.
- **Lettura File Esterno:** Room deve essere configurato utilizzando `createFromFile()` o `createFromAsset()` per agganciarsi al file `.db` esistente.
- **Mappatura Rigida:** Le `Entity` Room devono mappare esattamente le tabelle vecchie usando `@ColumnInfo` e `@TableName` per isolare le convenzioni di naming (es. snake_case o nomi non standard).

## 4. REGOLE DEL LAYER DI DOMINIO E MAPPERS
- **Isolamento della UI:** Un'entità Room (Entity) non deve **MAI** superare i confini del layer Data.
- **Mappers Obbligatori:** Devono sempre esistere funzioni di estensione (Mappers) che trasformano le `Entity` in modelli di dominio puliti (DTO) prima di passarli ai ViewModel. Questo è cruciale per standardizzare i payload in vista di future integrazioni API (es. PocketBase).

## 5. REGOLE DEL LAYER DI PRESENTAZIONE E COMPOSE
- **State Hoisting:** I componenti UI Compose devono essere primariamente "Stateless" (stupidi). Ricevono i dati tramite parametri e delegano gli eventi in alto tramite lambda (es. `onClick: () -> Unit`).
- **Gestione Stato:** Lo stato della singola schermata è gestito unicamente dal suo `ViewModel` esposto tramite un singolo `StateFlow<UiState>`.
- **Anteprime:** Ogni componente visivo isolato deve essere accompagnato dalla sua annotazione `@Preview`.

## 6. WORKFLOW DI SVILUPPO (Fette Verticali)
- Lo sviluppo procede sempre per "Vertical Slices" (Fette Verticali) o MVP incrementali.
- Non generare interi layer orizzontali (es. "tutti i DAO"). Implementare un'entità/funzionalità per volta: dal DB -> Mapper -> UseCase -> ViewModel -> Compose UI.
- Fase 1 dell'MVP: Implementazione rigorosamente in **sola lettura**.