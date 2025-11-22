-- db tidy, fix corrupt indices
REINDEX;

-- Yahoo stock URL correction #7736
UPDATE INFOTABLE_V1 SET INFOVALUE="https://finance.yahoo.com/quote/%s" WHERE INFONAME="STOCKURL" AND INFOVALUE="http://finance.yahoo.com/echarts?s=%s";

-- Fix currency format for Hungarian Forint #6128
UPDATE CURRENCYFORMATS_V1 SET SFX_SYMBOL=PFX_SYMBOL, PFX_SYMBOL=""  WHERE CURRENCY_SYMBOL="HUF" AND SFX_SYMBOL="";