-- Account Transactions list
WITH RECURSIVE categories(categid, categname, parentid) AS
    (SELECT a.categid, a.categname, a.parentid FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.parentid
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
SELECT     TX.TransID AS ID,
    TX.TransCode AS TransactionType,
    date( TX.TransDate ) AS Date,
    d.userdate AS UserDate,
    CAT.categName AS Category, -- Wolfsolver set full category name
    NULL AS Subcategory,       -- Wolfsolver ignore subcategory
    TX.Status AS Status,
    TX.NOTES AS Notes,
    ifnull(cfTo.BaseConvRate, cf.BaseConvRate) AS BaseConvRate,
    ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) *  TX.TransAmount AS Amount,
    FromAcc.CurrencyID AS CurrencyID,
    cf.currency_symbol AS currency,
    FromAcc.AccountID AS AccountID,
    FromAcc.AccountName AS AccountName,
    -- Destination
    ifnull(ToAcc.AccountId, FromAcc.AccountId) AS ToAccountId,
    ifnull(ToAcc.AccountName, FromAcc.AccountName) AS ToAccountName,
    TX.ToTransAmount AS ToAmount,
    ifnull(ToAcc.CurrencyId, FromAcc.CurrencyID) AS ToCurrencyId,
    ( CASE ifnull( TX.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS SPLITTED,
    -1 AS ParentCategID, -- Wolfsolver ignore subcategory (we use full category)
    TX.CATEGID AS CategID,
    -1 AS SubcategID,  -- Wolfsolver ignore subcategory (we use full category)
    ifnull( PAYEE.PayeeName, '') AS Payee,
    ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
    TX.TRANSACTIONNUMBER AS TransactionNumber,
    d.year AS Year,
    d.month AS Month,
    d.day AS Day,
    d.finyear AS finyear
FROM CHECKINGACCOUNT_V1 TX
    LEFT JOIN categories CAT ON CAT.CATEGID = TX.CATEGID
    LEFT JOIN categories PARENTCAT ON PARENTCAT.CATEGID = CAT.PARENTID
    LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = TX.PAYEEID
    LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = TX.ACCOUNTID
    LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = TX.TOACCOUNTID
    LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid
    LEFT JOIN currencyformats_v1 cfTo ON cfTo.currencyid = TOACC.currencyid
    LEFT JOIN (
        SELECT    transid AS id,
            date( transdate ) AS transdate,
            round( strftime( '%d', transdate ) ) AS day,
            round( strftime( '%m', transdate ) ) AS month,
            round( strftime( '%Y', transdate ) ) AS year,
            round( strftime( '%Y', transdate, 'start of month', ( (CASE WHEN fd.infovalue <= round( strftime( '%d', transdate ) ) THEN 1 ELSE 0 END ) - fm.infovalue ) || ' month' ) ) AS finyear,
            ifnull( ifnull( strftime( df.infovalue, TransDate ), ( strftime( REPLACE( df.infovalue, '%y', SubStr( strftime( '%Y', TransDate ), 3, 2 ) ), TransDate ) ) ), date( TransDate ) ) AS UserDate
        FROM CHECKINGACCOUNT_V1 LEFT JOIN infotable_v1 df ON df.infoname = 'DATEFORMAT'
            LEFT JOIN infotable_v1 fm ON fm.infoname = 'FINANCIAL_YEAR_START_MONTH'
            LEFT JOIN infotable_v1 fd ON fd.infoname = 'FINANCIAL_YEAR_START_DAY'
    ) d ON d.id = TX.TRANSID
WHERE (TX.DELETEDTIME IS NULL OR TX.DELETEDTIME = '')
