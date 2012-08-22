CREATE TABLE IF NOT EXISTS ACCOUNTLIST_V1(
ACCOUNTID integer primary key,
ACCOUNTNAME TEXT NOT NULL,
ACCOUNTTYPE TEXT NOT NULL,
ACCOUNTNUM TEXT,
STATUS TEXT NOT NULL, 
NOTES TEXT,
HELDAT TEXT,
WEBSITE TEXT,
CONTACTINFO TEXT,
ACCESSINFO TEXT,
INITIALBAL numeric,
FAVORITEACCT TEXT NOT NULL,
CURRENCYID integer NOT NULL);

CREATE TABLE IF NOT EXISTS ASSETS_V1(ASSETID integer primary key,
STARTDATE TEXT NOT NULL,
ASSETNAME TEXT,
VALUE numeric,
VALUECHANGE TEXT,
NOTES TEXT,
VALUECHANGERATE numeric,
ASSETTYPE TEXT);

CREATE TABLE IF NOT EXISTS BILLSDEPOSITS_V1(BDID integer primary key,
ACCOUNTID integer NOT NULL,
TOACCOUNTID integer,
PAYEEID integer NOT NULL,
TRANSCODE TEXT NOT NULL,
TRANSAMOUNT numeric NOT NULL,
STATUS TEXT,
TRANSACTIONNUMBER TEXT,
NOTES TEXT,
CATEGID integer,
SUBCATEGID integer,
TRANSDATE TEXT,
FOLLOWUPID integer,
TOTRANSAMOUNT numeric,
REPEATS numeric,
NEXTOCCURRENCEDATE TEXT,
NUMOCCURRENCES numeric);

CREATE TABLE IF NOT EXISTS BUDGETSPLITTRANSACTIONS_V1(
SPLITTRANSID integer primary key,
TRANSID integer NOT NULL,
CATEGID integer,
SUBCATEGID integer,
SPLITTRANSAMOUNT numeric);

CREATE TABLE IF NOT EXISTS BUDGETTABLE_V1(
BUDGETENTRYID integer primary key,
BUDGETYEARID integer,
CATEGID integer,
SUBCATEGID integer,
PERIOD TEXT NOT NULL,
AMOUNT numeric NOT NULL);

CREATE TABLE IF NOT EXISTS BUDGETYEAR_V1(
BUDGETYEARID integer primary key,
BUDGETYEARNAME TEXT NOT NULL);

CREATE TABLE IF NOT EXISTS CATEGORY_V1(
CATEGID integer primary key,
CATEGNAME TEXT NOT NULL);

CREATE TABLE IF NOT EXISTS CHECKINGACCOUNT_V1(
TRANSID integer primary key,
ACCOUNTID integer NOT NULL,
TOACCOUNTID integer,
PAYEEID integer NOT NULL,
TRANSCODE TEXT NOT NULL,
TRANSAMOUNT numeric NOT NULL,
STATUS TEXT,
TRANSACTIONNUMBER TEXT,
NOTES TEXT,
CATEGID integer,
SUBCATEGID integer,
TRANSDATE TEXT,
FOLLOWUPID integer,
TOTRANSAMOUNT numeric);

CREATE TABLE IF NOT EXISTS CURRENCYFORMATS_V1(
CURRENCYID integer primary key,
CURRENCYNAME TEXT NOT NULL,
PFX_SYMBOL TEXT,
SFX_SYMBOL TEXT,
DECIMAL_POINT TEXT,
GROUP_SEPARATOR TEXT,
UNIT_NAME TEXT,
CENT_NAME TEXT,
SCALE numeric,
BASECONVRATE numeric,
CURRENCY_SYMBOL TEXT);
INSERT INTO "CURRENCYFORMATS_V1" VALUES(1,'US DOLLAR','$','','.',',','dollar','cents',100,1,'USD');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(2,'EURO','','€',',','.','','',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(3,'EURO-FRANCE','','€',',',' ','','',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(4,'EURO-BELGIUM','€','',',','.','','',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(5,'EURO-ITALY','€','',',','.','','',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(6,'UK POUND','£','','.',',','Pound','Pence',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(7,'SWISS FRANC','CHF','','.',',','franc','centimes',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(8,'RUB','','р.',',','','Рубль','коп.',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(9,'UAH','','грн.',',',' ','Гривна','коп.',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(10,'INS','ש״ח','','.',',','Israeli new shekel','',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(11,'POLSKI ZŁOTY','',' zł',',',' ','złoty','grosz',100,1,'');
INSERT INTO "CURRENCYFORMATS_V1" VALUES(12,'AU DOLLAR','$','','.',',','Dollar','Cent',100,1,'');

CREATE TABLE IF NOT EXISTS INFOTABLE_V1 (
INFOID integer not null primary key,
INFONAME TEXT NOT NULL,
INFOVALUE TEXT NOT NULL );
INSERT INTO "INFOTABLE_V1" VALUES(1,'MMEXVERSION','0.9.8.0');
INSERT INTO "INFOTABLE_V1" VALUES(2,'DATAVERSION','2');
INSERT INTO "INFOTABLE_V1" VALUES(3,'CREATEDATE','2011-12-28');
INSERT INTO "INFOTABLE_V1" VALUES(4,'DATEFORMAT','%m/%d/%y');
INSERT INTO "INFOTABLE_V1" VALUES(5,'BASECURRENCYID','5');
INSERT INTO "INFOTABLE_V1" VALUES(6,'USERNAME','');

CREATE TABLE IF NOT EXISTS PAYEE_V1(
PAYEEID integer primary key,
PAYEENAME TEXT NOT NULL,
CATEGID integer,
SUBCATEGID integer);

CREATE TABLE IF NOT EXISTS SPLITTRANSACTIONS_V1(
SPLITTRANSID integer primary key,
TRANSID numeric NOT NULL,
CATEGID integer,
SUBCATEGID integer,
SPLITTRANSAMOUNT numeric);

CREATE TABLE IF NOT EXISTS STOCK_V1(
STOCKID integer primary key,
HELDAT numeric,
PURCHASEDATE TEXT NOT NULL,
STOCKNAME TEXT,
SYMBOL TEXT,
NUMSHARES numeric,
PURCHASEPRICE numeric NOT NULL,
NOTES TEXT,
CURRENTPRICE numeric NOT NULL,
VALUE numeric,
COMMISSION numeric);

CREATE TABLE IF NOT EXISTS SUBCATEGORY_V1(
SUBCATEGID integer primary key,
SUBCATEGNAME TEXT NOT NULL,
CATEGID integer NOT NULL);

DROP VIEW IF EXISTS alldata;

CREATE VIEW IF NOT EXISTS alldata AS
       SELECT CANS.TransID AS ID,
              CANS.TransCode AS TransactionType,
              date( CANS.TransDate, 'localtime' ) AS Date,
              d.userdate AS UserDate,
              coalesce( CAT.CategName, SCAT.CategName ) AS Category,
              coalesce( SUBCAT.SUBCategName, SSCAT.SUBCategName, '' ) AS Subcategory,
              ROUND( ( CASE CANS.TRANSCODE 
                       WHEN 'Withdrawal' THEN -1 
                       ELSE 1 
              END ) *  ( CASE CANS.CATEGID 
                       WHEN -1 THEN st.splittransamount 
                       ELSE CANS.TRANSAMOUNT 
              END ) , 2 ) AS Amount,
              cf.currency_symbol AS currency,
              CANS.Status AS Status,
              CANS.NOTES AS Notes,
              cf.BaseConvRate AS BaseConvRate,
              FROMACC.CurrencyID AS CurrencyID,
              FROMACC.AccountName AS AccountName,
              FROMACC.AccountID AS AccountID,
              ifnull( TOACC.AccountName, '' ) AS ToAccountName,
              ifnull( TOACC.ACCOUNTId, -1 ) AS ToAccountID,
              CANS.ToTransAmount ToTransAmount,
              ifnull( TOACC.CURRENCYID, -1 ) AS ToCurrencyID,
              ( CASE ifnull( CANS.CATEGID, -1 ) 
                       WHEN -1 THEN 1 
                       ELSE 0 
              END ) AS Splitted,
              ifnull( CAT.CategId, st.CategId ) AS CategID,
              ifnull( ifnull( SUBCAT.SubCategID, st.subCategId ) , -1 ) AS SubCategID,
              ifnull( PAYEE.PayeeName, '' ) AS Payee,
              ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
              CANS.TRANSACTIONNUMBER AS TransactionNumber,
              d.year AS Year,
              d.month AS Month,
              d.day AS Day,
              d.finyear AS FinYear
         FROM CHECKINGACCOUNT_V1 CANS
              LEFT JOIN CATEGORY_V1 CAT
                     ON CAT.CATEGID = CANS.CATEGID
              LEFT JOIN SUBCATEGORY_V1 SUBCAT
                     ON SUBCAT.SUBCATEGID = CANS.SUBCATEGID 
       AND
       SUBCAT.CATEGID = CANS.CATEGID
              LEFT JOIN PAYEE_V1 PAYEE
                     ON PAYEE.PAYEEID = CANS.PAYEEID
              LEFT JOIN ACCOUNTLIST_V1 FROMACC
                     ON FROMACC.ACCOUNTID = CANS.ACCOUNTID
              LEFT JOIN ACCOUNTLIST_V1 TOACC
                     ON TOACC.ACCOUNTID = CANS.TOACCOUNTID
              LEFT JOIN splittransactions_v1 st
                     ON CANS.transid = st.transid
              LEFT JOIN CATEGORY_V1 SCAT
                     ON SCAT.CATEGID = st.CATEGID 
       AND
       CANS.TransId = st.transid
              LEFT JOIN SUBCATEGORY_V1 SSCAT
                     ON SSCAT.SUBCATEGID = st.SUBCATEGID 
       AND
       SSCAT.CATEGID = st.CATEGID 
       AND
       CANS.TransId = st.transid
              LEFT JOIN currencyformats_v1 cf
                     ON cf.currencyid = FROMACC.currencyid
              LEFT JOIN  ( 
           SELECT transid AS id,
                  date( transdate, 'localtime' ) AS transdate,
                  round( strftime( '%d', transdate, 'localtime' )  ) AS day,
                  round( strftime( '%m', transdate, 'localtime' )  ) AS month,
                  round( strftime( '%Y', transdate, 'localtime' )  ) AS year,
                  round( strftime( '%Y', transdate, 'localtime', 'start of month',  (  ( CASE
                               WHEN fd.infovalue <= round( strftime( '%d', transdate, 'localtime' )  ) THEN 1 
                               ELSE 0 
                  END ) - fm.infovalue ) || ' month' )  ) AS finyear,
                  ifnull( ifnull( strftime( df.infovalue, TransDate, 'localtime' ) ,  ( strftime( REPLACE( df.infovalue, '%y', SubStr( strftime( '%Y', TransDate, 'localtime' ) , 3, 2 )  ) , TransDate, 'localtime' )  )  ) , date( TransDate, 'localtime' )  ) AS UserDate
             FROM CHECKINGACCOUNT_V1
                  LEFT JOIN infotable_v1 df
                         ON df.infoname = 'DATEFORMAT'
                  LEFT JOIN infotable_v1 fm
                         ON fm.infoname = 'FINANCIAL_YEAR_START_MONTH'
                  LEFT JOIN infotable_v1 fd
                         ON fd.infoname = 'FINANCIAL_YEAR_START_DAY' 
       ) 
       d
                     ON d.id = CANS.TRANSID
        ORDER BY CANS.transid;
		
CREATE TABLE IF NOT EXISTS "android_metadata" ("locale" TEXT DEFAULT 'en_US');