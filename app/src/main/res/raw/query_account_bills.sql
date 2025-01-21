-- Accounts with balances
-- This query is used for All Accounts widget and the balances in the Home screen.
SELECT
    ACCOUNTLIST_V1.ACCOUNTID AS _id,
    ACCOUNTLIST_V1.ACCOUNTID,
    ACCOUNTLIST_V1.ACCOUNTNAME,
    ACCOUNTLIST_V1.STATUS,
    ACCOUNTLIST_V1.FAVORITEACCT,
    ACCOUNTLIST_V1.CURRENCYID,
    ACCOUNTLIST_V1.ACCOUNTTYPE,
    (INITIALBAL + ifnull(T1.TOTAL, 0)) AS TOTAL,
    (INITIALBAL + ifnull(T1.reconciled, 0)) AS RECONCILED,
    (INITIALBAL + ifnull(T1.TOTAL, 0)) * ifnull(CURRENCYFORMATS_V1.BASECONVRATE, 1) AS TOTALBASECONVRATE,
    (INITIALBAL + ifnull(T1.reconciled, 0)) * ifnull(CURRENCYFORMATS_V1.BASECONVRATE, 1) AS RECONCILEDBASECONVRATE
FROM ACCOUNTLIST_V1 LEFT JOIN ( 
    select accountid, SUM(total) as total, SUM(reconciled) as reconciled
    from (
        -- Withdrawals
        select accountid, transcode, sum(case when status in ('R', 'F', 'D', '') then -transamount else 0 end) as total,
            sum(case when status = 'R' then -transamount else 0 end) as reconciled
        from checkingaccount_v1
        where transcode in ('Withdrawal') and (deletedtime is null or deletedtime = '')
        group by accountid, transcode

        union all

        select accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then transamount else 0 end) as total,
            sum(case when status = 'R' then transamount else 0 end) as reconciled
        from checkingaccount_v1
        where transcode in ('Deposit') and (deletedtime is null or deletedtime = '')
        group by accountid, transcode

        union all

        select accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then -transamount else 0 end) as total,
            sum(case when status = 'R' then -transamount else 0 end) as reconciled
        from checkingaccount_v1
        where transcode in ('Transfer') and (deletedtime is null or deletedtime = '')
        group by accountid, transcode

        union all

        select toaccountid AS accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then totransamount else 0 end) as total,
            sum(case when status = 'R' then totransamount else 0 end) as reconciled
        from checkingaccount_v1
        where transcode in ('Transfer') and toaccountid <> -1 and (deletedtime is null or deletedtime = '')
        group by toaccountid, transcode

        union all

        -- Investments
        select HeldAt as accountid,
            'Deposit' as transcode,
            sum(NumShares * CurrentPrice) as total,
            sum(NumShares * CurrentPrice) as reconciled
        from stock_v1
        group by accountid, transcode

    )  t
    group by accountid
) T1 ON ACCOUNTLIST_V1.ACCOUNTID = T1.ACCOUNTID 
LEFT JOIN CURRENCYFORMATS_V1 ON ACCOUNTLIST_V1.CURRENCYID = CURRENCYFORMATS_V1.CURRENCYID
