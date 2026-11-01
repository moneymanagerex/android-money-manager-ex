# Baseline 10: Feature - Investments and Currencies

Logic for stock markets, investment accounts, and multi-currency conversion.

## 1. Stock Portfolio and Investments (`investment/`)
Unlike basic checking accounts, an "Investment" account deals with Shares and Stocks.
- **`PortfolioFragment`**: Displays the investor dashboard, aggregating `SHAREINFO_V1` (share quantity) multiplied by today's quote extrapolated from `STOCKHISTORY_V1`.
- **`StockTransactionListActivity`**: A special ledger where transactions are not simple incoming/outgoing amounts, but Buy, Sell, Stock Split, or Dividend (mapped via special status/type).
- **`PriceEditActivity`**: Allows manual insertion of the quote for a stock or ETF on a specific date.

## 2. Quote Downloader (`prices/`, `yahoofinance/`)
The app attempts to automatically obtain the Net Asset Value (NAV) of entered stocks.
- **`YahooChartDownloaderRetrofit`**: Historical implementation (updated to Retrofit) to contact undocumented public APIs (or AlphaVantage/equivalents) to update the portfolio value in the background.
- **`SecurityPriceUpdaterFactory`**: Factory pattern to return the right update service (manual, Yahoo, custom API) based on the stock.

## 3. Currencies and Exchange Rates Management (`currency/`)
MMEX has a base currency (e.g., USD) but supports infinite currencies for accounts.
- **`CurrencyService`**: Exposes the mathematical logic of multiplication and division between rates, drawing from the `CURRENCYHISTORY_V1` table. Particularly critical in Transfers between two accounts with different currencies.
- **`CurrencyUIFeatures`**: Utility class (or ViewModel/Helper) used by all UI adapters to pre-format a string (e.g., "$1,000.50" versus "1.000,50 €") based on the currency indicated on `ACCOUNTLIST_V1`.
- **`ExchangeRateUpdaterFactory` / `FreeCurrencyExchangeRateAPIService`**: Like for stocks, it delegates the retrieval of today's conversion rate (relative to the base currency) to REST web API services via asynchronous Retrofit calls.