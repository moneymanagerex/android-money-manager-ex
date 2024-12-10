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
    CAT.categName AS Category, -- Wolfsolver set full category name
    NULL AS Subcategory,       -- Wolfsolver ignore subcategory
    TX.Status AS Status,
    TX.NOTES AS Notes,
    ifnull(cf.BaseConvRate, cfTo.BaseConvRate) AS BaseConvRate,
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
    TX.CATEGID AS CategID,
    ifnull( PAYEE.PayeeName, '') AS PayeeName,
    ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
    TX.TRANSACTIONNUMBER AS TransactionNumber,

    ATT.ATTACHMENTCOUNT AS ATTACHMENTCOUNT,
    round( strftime( '%d', TX.transdate ) ) AS day,
    round( strftime( '%m', TX.transdate ) ) AS month,
    round( strftime( '%Y', TX.transdate ) ) AS year

FROM CHECKINGACCOUNT_V1 TX
    LEFT JOIN categories CAT ON CAT.CATEGID = TX.CATEGID
    LEFT JOIN categories PARENTCAT ON PARENTCAT.CATEGID = CAT.PARENTID
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
    ) AS ATT on TX.TransID and ATT.REFID
WHERE (TX.DELETEDTIME IS NULL OR TX.DELETEDTIME = '')
