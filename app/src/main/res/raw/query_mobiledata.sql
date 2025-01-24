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
    COALESCE( SCAT.categname, CAT.categname, "" ) AS Category,  -- was FullCatgName
    cf.currency_symbol AS currency,
    TX.Status AS Status,
    TX.NOTES AS Notes,
    ifnull(cf.BaseConvRate, cfTo.BaseConvRate) AS BaseConvRate,
    ROUND( ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) *
        ( CASE ifnull( st.CATEGID, -1) WHEN -1 THEN TX.TRANSAMOUNT ELSE st.splittransamount END) , 2 ) AS Amount,
    FROMACC.CurrencyID AS CurrencyID,
    FROMACC.AccountName AS AccountName,
    FROMACC.AccountID AS AccountID,
    ifnull( TOACC.AccountName, '' ) AS ToAccountName,
    ifnull( TX.TOACCOUNTID, -1 ) AS ToAccountID,
    TX.ToTransAmount AS ToAmount,
    ifnull( TOACC.CURRENCYID, -1 ) AS ToCurrencyID,
    ( CASE ifnull( ST.CATEGID, -1 ) WHEN -1 THEN 0 ELSE 1 END ) AS SPLITTED,
    coalesce( st.CategId, TX.CategId, -1 ) AS CATEGID,
    ifnull( PAYEE.PayeeName, '') AS PayeeName,
    ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
    TX.TRANSACTIONNUMBER AS TransactionNumber,
    round( strftime( '%d', TX.transdate ) ) AS day,
    round( strftime( '%m', TX.transdate ) ) AS month,
    round( strftime( '%Y', TX.transdate ) ) AS year,
    ATT.ATTACHMENTCOUNT AS ATTACHMENTCOUNT,
    ROUND( ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) *
	  ( CASE ifnull( st.CATEGID, -1) WHEN -1 THEN TX.TRANSAMOUNT ELSE st.splittransamount END) , 2 )
        * ifnull(cf.BaseConvRate, 1) As AmountBaseConvRate,
	Tags.Tags as TAGS,
	TX.Color AS COLOR
FROM CHECKINGACCOUNT_V1 TX
    LEFT JOIN categories CAT ON CAT.CATEGID = TX.CATEGID
    LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = TX.PAYEEID
    LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = TX.ACCOUNTID
    LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = TX.TOACCOUNTID
    LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid
    LEFT JOIN currencyformats_v1 cfTo ON cfTo.currencyid = TOACC.currencyid
    LEFT JOIN splittransactions_v1 st ON TX.transid = st.transid
    LEFT JOIN categories SCAT ON SCAT.CATEGID = st.CATEGID AND TX.TransId = st.transid
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
    ) as TAGS on TX.Transid = TAGS.Transid
WHERE (TX.DELETEDTIME IS NULL OR TX.DELETEDTIME = '')
