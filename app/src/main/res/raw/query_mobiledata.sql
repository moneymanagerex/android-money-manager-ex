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
    COALESCE( SCAT.categname, CAT.categname ) AS CategoryFullName,
    COALESCE( SCAT.parentcategname, SCAT.catshortname, CAT.parentcategname, CAT.catshortname ) AS Category,
    COALESCE( SCAT.catshortname, CAT.catshortname ) AS Subcategory,
    cf.currency_symbol AS currency,
    TX.Status AS Status,
    TX.NOTES AS Notes,
    ifnull(cf.BaseConvRate, cfTo.BaseConvRate) AS BaseConvRate,
    ROUND( ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) *
        ( CASE TX.CATEGID WHEN -1 THEN st.splittransamount ELSE TX.TRANSAMOUNT END) , 2 ) AS Amount,
    FROMACC.CurrencyID AS CurrencyID,
    FROMACC.AccountName AS AccountName,
    FROMACC.AccountID AS AccountID,
    ifnull( TOACC.AccountName, '' ) AS ToAccountName,
    ifnull( TX.TOACCOUNTID, -1 ) AS ToAccountID,
    TX.ToTransAmount AS ToAmount,
    ifnull( TOACC.CURRENCYID, -1 ) AS ToCurrencyID,
    ( CASE ifnull( TX.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS SPLITTED,
    coalesce( SPARENTCAT.CATEGID, PARENTCAT.CATEGID, st.CategId, TX.CategId ) AS CATEGID,
    -1 AS SubcategID,
    ifnull( PAYEE.PayeeName, '') AS PayeeName,
    ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
    TX.TRANSACTIONNUMBER AS TransactionNumber,
    round( strftime( '%d', TX.transdate ) ) AS day,
    round( strftime( '%m', TX.transdate ) ) AS month,
    round( strftime( '%Y', TX.transdate ) ) AS year,
    ATT.ATTACHMENTCOUNT AS ATTACHMENTCOUNT,
    ROUND( ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) * ( CASE TX.CATEGID WHEN -1 THEN st.splittransamount ELSE TX.TRANSAMOUNT END) , 2 )
        * ifnull(cf.BaseConvRate, 1) As AmountBaseConvRate
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
    LEFT JOIN (
    select REFID, count(*) as ATTACHMENTCOUNT
    from ATTACHMENT_V1
    where REFTYPE = 'Transaction'
    group by REFID
    ) AS ATT on TX.TransID and ATT.REFID
WHERE (TX.DELETEDTIME IS NULL OR TX.DELETEDTIME = '')
