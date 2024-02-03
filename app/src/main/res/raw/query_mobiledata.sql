/*
    Query mobiledata. This is the base for most other queries.
*/
WITH RECURSIVE categories(categid, categname, catshortname, parentid, parentcategname ) AS
    (SELECT a.categid, a.categname, a.categname AS catshortname, a.parentid, NULL AS parentcategname FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.CATEGNAME AS catshortname, c.parentid, r.categname AS parentcategname
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
SELECT     TX.TransID AS ID,
    TX.TransCode AS TransactionType,
    date( TX.TransDate ) AS Date,
    d.userdate AS UserDate,
    coalesce( SCAT.categname, CAT.categname ) AS CategoryFullName,
    coalesce( SCAT.parentcategname, SCAT.catshortname, CAT.parentcategname, CAT.catshortname ) AS Category,
    coalesce( SCAT.catshortname, CAT.catshortname ) AS Subcategory,
    cf.currency_symbol AS currency,
    TX.Status AS Status,
    TX.NOTES AS Notes,
    ifnull(cfTo.BaseConvRate, cf.BaseConvRate) AS BaseConvRate,
    ROUND( ( CASE TX.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *
        ( CASE TX.CATEGID WHEN -1 THEN st.splittransamount ELSE TX.TRANSAMOUNT END) , 2 ) AS Amount,
    FROMACC.CurrencyID AS CurrencyID,
    FROMACC.AccountName AS AccountName,
    FROMACC.AccountID AS AccountID,
    ifnull( TOACC.AccountName, '' ) AS ToAccountName,
    ifnull( TOACC.ACCOUNTId, -1 ) AS ToAccountID,
    TX.ToTransAmount AS ToAmount,
    ifnull( TOACC.CURRENCYID, -1 ) AS ToCurrencyID,
    ( CASE ifnull( TX.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS SPLITTED,
    coalesce( SPARENTCAT.CATEGID, PARENTCAT.CATEGID, st.CategId, TX.CategId ) AS CATEGID,
    -1 AS SubcategID,
    ifnull( PAYEE.PayeeName, '') AS Payee,
    ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
    TX.TRANSACTIONNUMBER AS TransactionNumber,
    d.year AS Year,
    d.month AS Month,
    d.day AS Day,
    d.finyear AS finyear,
    ROUND( ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) * ( CASE TX.CATEGID WHEN -1 THEN st.splittransamount ELSE TX.TRANSAMOUNT END) , 2 )
        * ifnull(cfTo.BaseConvRate, 1) As AmountBaseConvRate
FROM CHECKINGACCOUNT_V1 TX
    LEFT JOIN categories CAT ON CAT.CATEGID = TX.CATEGID
--    LEFT JOIN categories PARENTCAT ON PARENTCAT.CATEGID = CAT.PARENTID
    LEFT JOIN categories PARENTCAT ON PARENTCAT.CATEGID = CAT.CATEGID
    LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = TX.PAYEEID
    LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = TX.ACCOUNTID
    LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = TX.TOACCOUNTID
    LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid
    LEFT JOIN currencyformats_v1 cfTo ON cfTo.currencyid = TOACC.currencyid
    LEFT JOIN splittransactions_v1 st ON TX.transid = st.transid
    LEFT JOIN categories SCAT ON SCAT.CATEGID = st.CATEGID AND TX.TransId = st.transid
    LEFT JOIN categories SPARENTCAT ON SPARENTCAT.CATEGID = SCAT.CATEGID
    LEFT JOIN  (
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
