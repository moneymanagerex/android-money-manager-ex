SELECT SUB2.Year, SUB2.Month, SUM(SUB2.i) AS Income, SUM(SUB2.e) AS Expenses, SUM(SUB2.t) AS Transfers
FROM (
select sub1.month, sub1.year,
case when lower(sub1.transactiontype)='deposit' then sub1.total else 0 end as i,
case when lower(sub1.transactiontype)='withdrawal' then sub1.total else 0 end as e,
case when lower(sub1.transactiontype)='transfer' then sub1.total else 0 end as t
from (
select mobiledata.month, mobiledata.year, mobiledata.transactiontype, sum(mobiledata.AmountBaseConvRate) as total
from (
SELECT 	CANS.TransID AS ID,
	CANS.TransCode AS TransactionType,
	date( CANS.TransDate ) AS Date,
	d.userdate AS UserDate,
	coalesce( CAT.CategName, SCAT.CategName ) AS Category,
	coalesce( SUBCAT.SUBCategName, SSCAT.SUBCategName, '' ) AS Subcategory,
	ROUND( ( CASE CANS.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  ( CASE CANS.CATEGID WHEN -1 THEN st.splittransamount ELSE CANS.TRANSAMOUNT END) , 2 ) AS Amount,
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
	( CASE ifnull( CANS.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS Splitted,
	ifnull( CAT.CategId, st.CategId ) AS CategID,
	ifnull( ifnull( SUBCAT.SubCategID, st.subCategId ) , -1 ) AS SubCategID,
	ifnull( PAYEE.PayeeName, '' ) AS Payee,
	ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
	CANS.TRANSACTIONNUMBER AS TransactionNumber,
	d.year AS Year,
	d.month AS Month,
	d.day AS Day,
	d.finyear AS FinYear,
	ROUND( ( CASE CANS.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  ( CASE CANS.CATEGID WHEN -1 THEN st.splittransamount ELSE CANS.TRANSAMOUNT END) , 2 ) * ifnull(cf.BaseConvRate, 1) As AmountBaseConvRate
FROM 	CHECKINGACCOUNT_V1 CANS LEFT JOIN CATEGORY_V1 CAT ON CAT.CATEGID = CANS.CATEGID
	LEFT JOIN SUBCATEGORY_V1 SUBCAT ON SUBCAT.SUBCATEGID = CANS.SUBCATEGID AND SUBCAT.CATEGID = CANS.CATEGID
	LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = CANS.PAYEEID 
	LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = CANS.ACCOUNTID
	LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = CANS.TOACCOUNTID
	LEFT JOIN splittransactions_v1 st ON CANS.transid = st.transid
	LEFT JOIN CATEGORY_V1 SCAT ON SCAT.CATEGID = st.CATEGID AND CANS.TransId = st.transid
	LEFT JOIN SUBCATEGORY_V1 SSCAT ON SSCAT.SUBCATEGID = st.SUBCATEGID AND SSCAT.CATEGID = st.CATEGID AND CANS.TransId = st.transid
	LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid
	LEFT JOIN  ( 
           SELECT	transid AS id,
	    	  			date( transdate ) AS transdate,
	    				round( strftime( '%d', transdate )  ) AS day,
	    				round( strftime( '%m', transdate )  ) AS month,
	    				round( strftime( '%Y', transdate )  ) AS year,
	    				round( strftime( '%Y', transdate, 'start of month',  (  ( CASE WHEN fd.infovalue <= round( strftime( '%d', transdate )  ) THEN 1 ELSE 0 END ) - fm.infovalue ) || ' month' )  ) AS finyear,
	    				ifnull( ifnull( strftime( df.infovalue, TransDate ) ,  ( strftime( REPLACE( df.infovalue, '%y', SubStr( strftime( '%Y', TransDate ) , 3, 2 )  ) , TransDate )  )  ) , date( TransDate )  ) AS UserDate
           FROM CHECKINGACCOUNT_V1 LEFT JOIN infotable_v1 df ON df.infoname = 'DATEFORMAT'
	    				LEFT JOIN infotable_v1 fm ON fm.infoname = 'FINANCIAL_YEAR_START_MONTH'
	    				LEFT JOIN infotable_v1 fd ON fd.infoname = 'FINANCIAL_YEAR_START_DAY' 
       ) d ON d.id = CANS.TRANSID
) mobiledata
where not(mobiledata.status = 'V')
group by month, year, transactiontype
) sub1
) SUB2
GROUP BY SUB2.Year, SUB2.Month

UNION
-- 	 The total for the year
SELECT SUB2.Year, 99 AS Month, SUM(SUB2.i) AS Income, SUM(SUB2.e) AS Expenses, SUM(SUB2.t) AS Transfers
FROM (
select sub1.month, sub1.year,
case when lower(sub1.transactiontype)='deposit' then sub1.total else 0 end as i,
case when lower(sub1.transactiontype)='withdrawal' then sub1.total else 0 end as e,
case when lower(sub1.transactiontype)='transfer' then sub1.total else 0 end as t
from (
select mobiledata.month, mobiledata.year, mobiledata.transactiontype, sum(mobiledata.AmountBaseConvRate) as total
from (
SELECT 	CANS.TransID AS ID,
	CANS.TransCode AS TransactionType,
	date( CANS.TransDate ) AS Date,
	d.userdate AS UserDate,
	coalesce( CAT.CategName, SCAT.CategName ) AS Category,
	coalesce( SUBCAT.SUBCategName, SSCAT.SUBCategName, '' ) AS Subcategory,
	ROUND( ( CASE CANS.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  ( CASE CANS.CATEGID WHEN -1 THEN st.splittransamount ELSE CANS.TRANSAMOUNT END) , 2 ) AS Amount,
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
	( CASE ifnull( CANS.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS Splitted,
	ifnull( CAT.CategId, st.CategId ) AS CategID,
	ifnull( ifnull( SUBCAT.SubCategID, st.subCategId ) , -1 ) AS SubCategID,
	ifnull( PAYEE.PayeeName, '' ) AS Payee,
	ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
	CANS.TRANSACTIONNUMBER AS TransactionNumber,
	d.year AS Year,
	d.month AS Month,
	d.day AS Day,
	d.finyear AS FinYear,
	ROUND( ( CASE CANS.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  ( CASE CANS.CATEGID WHEN -1 THEN st.splittransamount ELSE CANS.TRANSAMOUNT END) , 2 ) * ifnull(cf.BaseConvRate, 1) As AmountBaseConvRate
FROM 	CHECKINGACCOUNT_V1 CANS LEFT JOIN CATEGORY_V1 CAT ON CAT.CATEGID = CANS.CATEGID
	LEFT JOIN SUBCATEGORY_V1 SUBCAT ON SUBCAT.SUBCATEGID = CANS.SUBCATEGID AND SUBCAT.CATEGID = CANS.CATEGID
	LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = CANS.PAYEEID
	LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = CANS.ACCOUNTID
	LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = CANS.TOACCOUNTID
	LEFT JOIN splittransactions_v1 st ON CANS.transid = st.transid
	LEFT JOIN CATEGORY_V1 SCAT ON SCAT.CATEGID = st.CATEGID AND CANS.TransId = st.transid
	LEFT JOIN SUBCATEGORY_V1 SSCAT ON SSCAT.SUBCATEGID = st.SUBCATEGID AND SSCAT.CATEGID = st.CATEGID AND CANS.TransId = st.transid
	LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid
	LEFT JOIN  (
           SELECT	transid AS id,
	    	  			date( transdate ) AS transdate,
	    				round( strftime( '%d', transdate )  ) AS day,
	    				round( strftime( '%m', transdate )  ) AS month,
	    				round( strftime( '%Y', transdate )  ) AS year,
	    				round( strftime( '%Y', transdate, 'start of month',  (  ( CASE WHEN fd.infovalue <= round( strftime( '%d', transdate )  ) THEN 1 ELSE 0 END ) - fm.infovalue ) || ' month' )  ) AS finyear,
	    				ifnull( ifnull( strftime( df.infovalue, TransDate ) ,  ( strftime( REPLACE( df.infovalue, '%y', SubStr( strftime( '%Y', TransDate ) , 3, 2 )  ) , TransDate )  )  ) , date( TransDate )  ) AS UserDate
           FROM CHECKINGACCOUNT_V1 LEFT JOIN infotable_v1 df ON df.infoname = 'DATEFORMAT'
	    				LEFT JOIN infotable_v1 fm ON fm.infoname = 'FINANCIAL_YEAR_START_MONTH'
	    				LEFT JOIN infotable_v1 fd ON fd.infoname = 'FINANCIAL_YEAR_START_DAY'
       ) d ON d.id = CANS.TRANSID
) mobiledata
where not(mobiledata.status = 'V')
group by month, year, transactiontype
) sub1
) SUB2
GROUP BY SUB2.Year

