SELECT 	CANS.TransID AS ID,
	CANS.TransCode AS TransactionType,
	date( CANS.TransDate ) AS Date,
	d.userdate AS UserDate,
	CAT.CategName as Category,
	SUBCAT.SUBCategName as Subcategory,
	CASE
	    WHEN CANS.ToTransAmount = 0 THEN ROUND( ( CASE CANS.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  CANS.TransAmount, 2 )
	    ELSE ROUND( ( CASE CANS.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  CANS.ToTransAmount, 2 )
	END as Amount,
	ifnull(cfTo.currency_symbol, cf.currency_symbol) AS currency,
	CANS.Status AS Status,
	CANS.NOTES AS Notes,
	ifnull(cfTo.BaseConvRate, cf.BaseConvRate) AS BaseConvRate,
	ifnull(ToAcc.CurrencyID, FROMACC.CurrencyID) as CurrencyID,
	ifnull(ToAcc.AccountName, FROMACC.AccountName) as AccountName,
	ifnull(ToAcc.AccountID, FROMACC.AccountID) as AccountID,
	FromAcc.AccountName as FromAccountName,
	FromAcc.AccountId as FromAccountId,
	CANS.TransAmount * -1 as FromAmount,
	FromAcc.CurrencyId as FromCurrencyId,
	( CASE ifnull( CANS.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS Splitted,
	ifnull( CAT.CategId, -1 ) AS CategID,
	ifnull( SUBCAT.SubCategID, -1 ) AS SubCategID,
	ifnull( PAYEE.PayeeName, '') AS Payee,
	ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
	CANS.TRANSACTIONNUMBER AS TransactionNumber,
	d.year AS Year,
	d.month AS Month,
	d.day AS Day,
	d.finyear AS FinYear
FROM 	CHECKINGACCOUNT_V1 CANS 
	LEFT JOIN CATEGORY_V1 CAT ON CAT.CATEGID = CANS.CATEGID
	LEFT JOIN SUBCATEGORY_V1 SUBCAT ON SUBCAT.SUBCATEGID = CANS.SUBCATEGID AND SUBCAT.CATEGID = CANS.CATEGID
	LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = CANS.PAYEEID 
	LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = CANS.ACCOUNTID
	LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = CANS.TOACCOUNTID
	LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid
	LEFT JOIN currencyformats_v1 cfTo ON cfTo.currencyid = TOACC.currencyid
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
