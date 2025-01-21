-- Account Transactions list
WITH RECURSIVE categories(categid, categname, parentid) AS
    (SELECT a.categid, a.categname, a.parentid FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.parentid
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
SELECT 
    TX.TransID AS ID,
    TX.TransCode AS TransactionType,
    date( TX.TransDate ) AS Date,
    CAT.categName AS Category,
    TX.Status AS Status,
    TX.NOTES AS Notes,
    ifnull(cf.BaseConvRate, cfTo.BaseConvRate) AS BaseConvRate,
    ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) *  TX.TransAmount AS Amount,
    FromAcc.CurrencyID AS CurrencyID,
    cf.currency_symbol AS currency,
    FromAcc.AccountID AS AccountID,
    FromAcc.AccountName AS AccountName,
    -- Destination
    ifnull(ToAcc.AccountId, FromAcc.AccountId) AS ToAccountID,
    ifnull(ToAcc.AccountName, FromAcc.AccountName) AS ToAccountName,
    TX.ToTransAmount AS ToAmount,
    ifnull(ToAcc.CurrencyId, FromAcc.CurrencyID) AS ToCurrencyID,
    ( CASE ifnull( TX.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS SPLITTED,
    TX.CATEGID AS CategID,
    ifnull( PAYEE.PayeeName, '') AS PayeeName,
    ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
    TX.TRANSACTIONNUMBER AS TransactionNumber,
    ATT.ATTACHMENTCOUNT AS ATTACHMENTCOUNT,
    round( strftime( '%d', TX.transdate ) ) AS day,
    round( strftime( '%m', TX.transdate ) ) AS month,
    round( strftime( '%Y', TX.transdate ) ) AS year,
	Tags.Tags as TAGS
FROM CHECKINGACCOUNT_V1 TX
    LEFT JOIN categories CAT ON CAT.CATEGID = TX.CATEGID
    LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = TX.PAYEEID
    LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = TX.ACCOUNTID
    LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = TX.TOACCOUNTID
    LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid
    LEFT JOIN currencyformats_v1 cfTo ON cfTo.currencyid = TOACC.currencyid
    LEFT JOIN (
        select REFID, count(*) as ATTACHMENTCOUNT
        from ATTACHMENT_V1
        where REFTYPE = 'Transaction'
        group by REFID
    ) AS ATT on TX.TransID = ATT.REFID
    LEFT JOIN (
        select Transid, Tags from (
        SELECT TRANSACTIONID as Transid,
               group_concat(TAGNAME) AS Tags
        FROM (SELECT TAGLINK_V1.REFID as TRANSACTIONID, TAG_V1.TAGNAME
              FROM TAGLINK_V1 inner join TAG_V1 on TAGLINK_V1.TAGID = TAG_V1.TAGID
              where REFTYPE = "Transaction" and ACTIVE = 1
              ORDER BY REFID, TAGNAME)
        GROUP BY TRANSACTIONID)
    ) as TAGS on TX.TransID = TAGS.Transid
WHERE (TX.DELETEDTIME IS NULL OR TX.DELETEDTIME = '')
