/*
    Query mobiledata. This is the base for most other queries.
*/
WITH RECURSIVE categories(categid, categname, catshortname, parentid, parentcategname, fullcatid ) AS
    (SELECT a.categid, a.categname, a.categname AS catshortname, a.parentid, NULL AS parentcategname, ":" || a.categid || ":" FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.CATEGNAME AS catshortname, c.parentid, r.categname AS parentcategname, r.fullcatid || c.categid || ":"
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
SELECT
    TX.TransID AS ID,
    TX.TransCode AS TransactionType,
    date( TX.TransDate ) AS Date,
    COALESCE( SCAT.categname, CAT.categname, "" ) AS Category,
	coalesce( st.CategId, TX.CategId, -1 ) AS CategID,
    COALESCE( SCAT.fullcatid, CAT.fullcatid, "" ) AS FullCatID,
    TX.Status AS Status,
    CASE
        WHEN ST.NOTES = TX.NOTES THEN TX.NOTES
        ELSE COALESCE(TX.NOTES || ':' || NULLIF(ST.NOTES, ''), TX.NOTES)
    END AS Notes,
    ifnull(cf.BaseConvRate, cfTo.BaseConvRate) AS BaseConvRate,
    cf.currency_symbol AS currency,
    ROUND( ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) *
        ( CASE ifnull( st.CATEGID, -1) WHEN -1 THEN TX.TRANSAMOUNT ELSE st.splittransamount END) , 2 ) AS Amount,
    FROMACC.CurrencyID AS CurrencyID,
    FROMACC.AccountID AS AccountID,
    FROMACC.AccountName AS AccountName,
    FROMACC.ACCOUNTTYPE AS ACCOUNTTYPE,
    ifnull( TX.TOACCOUNTID, -1 ) AS ToAccountID,
    ifnull( TOACC.AccountName, '' ) AS ToAccountName,
	ifnull( TOACC.ACCOUNTTYPE, '' ) AS TOACCOUNTTYPE,
    TX.ToTransAmount AS ToAmount,
    ifnull( TOACC.CURRENCYID, -1 ) AS ToCurrencyID,
    ( CASE ifnull( ST.CATEGID, -1 ) WHEN -1 THEN 0 ELSE 1 END ) AS SPLITTED,
    ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
    ifnull( PAYEE.PayeeName, '') AS PayeeName,
    TX.TRANSACTIONNUMBER AS TransactionNumber,
    round( strftime( '%d', TX.transdate ) ) AS day,
    round( strftime( '%m', TX.transdate ) ) AS month,
    round( strftime( '%Y', TX.transdate ) ) AS year,
    ATT.ATTACHMENTCOUNT AS ATTACHMENTCOUNT,
	Tags.Tags AS TAGS,
	TX.Color AS COLOR,
    ROUND( ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) *
	  ( CASE ifnull( st.CATEGID, -1) WHEN -1 THEN TX.TRANSAMOUNT ELSE st.splittransamount END) , 2 )
        * ifnull(cf.BaseConvRate, 1) As AmountBaseConvRate
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
		SELECT Transid, Splitid, group_concat(TAGNAME) AS Tags
		FROM (
			SELECT TRANSACTIONID as Transid, SPLITTRANSID as Splitid, TAGNAME
			FROM (
				SELECT TAGLINK_V1.REFID as TRANSACTIONID, null as SPLITTRANSID, TAG_V1.TAGNAME
				FROM TAGLINK_V1
				INNER JOIN TAG_V1 ON TAGLINK_V1.TAGID = TAG_V1.TAGID
				WHERE REFTYPE = 'Transaction' AND ACTIVE = 1
				UNION
				-- Tag legati agli split delle transazioni (ST) mappati sul TransID principale
				SELECT null AS TRANSACTIONID, TAGLINK_V1.REFID as SPLITTRANSID, TAG_V1.TAGNAME
				FROM TAGLINK_V1
				INNER JOIN TAG_V1 ON TAGLINK_V1.TAGID = TAG_V1.TAGID
				WHERE REFTYPE = 'TransactionSplit' AND ACTIVE = 1
			)
			ORDER BY TRANSACTIONID, SPLITTRANSID, TAGNAME
		)
		GROUP BY Transid, Splitid
    ) as TAGS on ( TX.Transid = TAGS.Transid or ST.SPLITTRANSID = TAGS.Splitid )
WHERE (TX.DELETEDTIME IS NULL OR TX.DELETEDTIME = '')
