-- Account Transactions list
SELECT 	TX.TransID AS ID,
	TX.TransCode AS TransactionType,
	date( TX.TransDate ) AS Date,
	d.userdate AS UserDate,
	CAT.CategName as Category,
	SUBCAT.SUBCategName as Subcategory,
--	CASE
--	    WHEN TX.TransAmount = 0
--	    THEN ( CASE TX.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  TX.ToTransAmount
--	    ELSE ( CASE TX.TRANSCODE WHEN 'Withdrawal' THEN -1 ELSE 1 END ) *  TX.TransAmount
--	END as Amount,
	TX.Status AS Status,
	TX.NOTES AS Notes,
	ifnull(cfTo.BaseConvRate, cf.BaseConvRate) AS BaseConvRate,
	-- Source
    -- Withdrawals and Transfers have negative sign.
    ( CASE TX.TRANSCODE WHEN 'Deposit' THEN 1 ELSE -1 END ) *  TX.TransAmount as Amount,
	FromAcc.CurrencyID as CurrencyID,
	cf.currency_symbol AS currency,
	FromAcc.AccountID as AccountID,
	FromAcc.AccountName as AccountName,
	-- Destination
	ifnull(ToAcc.AccountId, FromAcc.AccountId) as ToAccountId,
	ifnull(ToAcc.AccountName, FromAcc.AccountName) as ToAccountName,
	TX.ToTransAmount as ToAmount,
	ifnull(ToAcc.CurrencyId, FromAcc.CurrencyID) as ToCurrencyId,
	( CASE ifnull( TX.CATEGID, -1 ) WHEN -1 THEN 1 ELSE 0 END ) AS SPLITTED,
	ifnull( CAT.CategId, -1 ) AS CategID,
	ifnull( SUBCAT.SubCategID, -1 ) AS SubcategID,
	ifnull( PAYEE.PayeeName, '') AS Payee,
	ifnull( PAYEE.PayeeID, -1 ) AS PayeeID,
	TX.TRANSACTIONNUMBER AS TransactionNumber,
	d.year AS Year,
	d.month AS Month,
	d.day AS Day,
	d.finyear AS finyear
FROM CHECKINGACCOUNT_V1 TX 
	LEFT JOIN CATEGORY_V1 CAT ON CAT.CATEGID = TX.CATEGID
	LEFT JOIN SUBCATEGORY_V1 SUBCAT ON SUBCAT.SUBCATEGID = TX.SUBCATEGID AND SUBCAT.CATEGID = TX.CATEGID
	LEFT JOIN PAYEE_V1 PAYEE ON PAYEE.PAYEEID = TX.PAYEEID 
	LEFT JOIN ACCOUNTLIST_V1 FROMACC ON FROMACC.ACCOUNTID = TX.ACCOUNTID
	LEFT JOIN ACCOUNTLIST_V1 TOACC ON TOACC.ACCOUNTID = TX.TOACCOUNTID
	LEFT JOIN currencyformats_v1 cf ON cf.currencyid = FROMACC.currencyid
	LEFT JOIN currencyformats_v1 cfTo ON cfTo.currencyid = TOACC.currencyid
	LEFT JOIN (
        SELECT	transid AS id,
			date( transdate ) AS transdate,
			round( strftime( '%d', transdate )  ) AS day,
			round( strftime( '%m', transdate )  ) AS month,
			round( strftime( '%Y', transdate )  ) AS year,
			round( strftime( '%Y', transdate, 'start of month', ( (CASE WHEN fd.infovalue <= round( strftime( '%d', transdate )  ) THEN 1 ELSE 0 END ) - fm.infovalue ) || ' month' )  ) AS finyear,
			ifnull( ifnull( strftime( df.infovalue, TransDate ) ,  ( strftime( REPLACE( df.infovalue, '%y', SubStr( strftime( '%Y', TransDate ) , 3, 2 )  ) , TransDate )  )  ) , date( TransDate )  ) AS UserDate
	    FROM CHECKINGACCOUNT_V1 LEFT JOIN infotable_v1 df ON df.infoname = 'DATEFORMAT'
		LEFT JOIN infotable_v1 fm ON fm.infoname = 'FINANCIAL_YEAR_START_MONTH'
		LEFT JOIN infotable_v1 fd ON fd.infoname = 'FINANCIAL_YEAR_START_DAY'
    ) d ON d.id = TX.TRANSID
