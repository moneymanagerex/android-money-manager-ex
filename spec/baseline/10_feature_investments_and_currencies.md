# Baseline 10: Feature - Investments and Currencies

Logica per mercati azionari, conti investimento e conversione multivaluta.

## 1. Portafoglio Titoli e Investimenti (`investment/`)
Contrariamente ai conti correnti base, un conto di tipo "Investimento" tratta Quote e Azioni.
- **`PortfolioFragment`**: Visualizza la dashboard dell'investitore, aggregando le `SHAREINFO_V1` (quantità azionaria) moltiplicate per la quotazione odierna estrapolata da `STOCKHISTORY_V1`.
- **`StockTransactionListActivity`**: Un registro speciale in cui le transazioni non sono semplici importi in entrata/uscita, ma Buy, Sell, Split Azionario o Dividendo (mappati tramite status/tipo speciali).
- **`PriceEditActivity`**: Permette l'inserimento manuale della quotazione di un titolo o ETF in una data precisa.

## 2. Downloader Quotazioni (`prices/`, `yahoofinance/`)
L'app cerca di ottenere automaticamente il Net Asset Value (NAV) delle azioni inserite.
- **`YahooChartDownloaderRetrofit`**: Storica implementazione (aggiornata a Retrofit) per contattare le API pubbliche non documentate (o AlphaVantage/equivalenti) per aggiornare il valore del portafoglio in background.
- **`SecurityPriceUpdaterFactory`**: Pattern Factory per restituire il giusto servizio di update (manuale, Yahoo, API custom) in base al titolo.

## 3. Gestione Valute e Tassi di Cambio (`currency/`)
MMEX ha una base valuta (es. USD) ma supporta infinite valute per i conti.
- **`CurrencyService`**: Espone la logica matematica di moltiplicazione e divisione tra tassi, attingendo alla tabella `CURRENCYHISTORY_V1`. Particolarmente critico nei Trasferimenti tra due conti con valute differenti.
- **`CurrencyUIFeatures`**: Utility classe (o ViewModel/Helper) usata da tutti gli adapter UI per pre-formattare una stringa (es. "$1,000.50" contro "1.000,50 €") sulla base della valuta indicata su `ACCOUNTLIST_V1`.
- **`ExchangeRateUpdaterFactory` / `FreeCurrencyExchangeRateAPIService`**: Come per le azioni, delega il recupero del tasso di conversione odierno (rispetto alla base valuta) a servizi API web REST tramite chiamate Retrofit asincrone.
