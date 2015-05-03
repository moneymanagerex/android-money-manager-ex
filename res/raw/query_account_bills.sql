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
FROM ACCOUNTLIST_V1 LEFT OUTER JOIN ( 
select accountid, ROUND(SUM(total), 2) as total, ROUND(SUM(reconciled), 2) as reconciled 
from ( 
    select accountid, transcode, sum(case when status in ('R', 'F', 'D', '') then -transamount else 0 end) as total, sum(case when status = 'R' then -transamount else 0 end) as reconciled
    from checkingaccount_v1
    where transcode in ('Withdrawal')
    group by accountid, transcode
 
    union

    select accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then transamount else 0 end) as total, sum(case when status = 'R' then transamount else 0 end) as reconciled
    from checkingaccount_v1
    where transcode in ('Deposit')
    group by accountid, transcode

    union

    select accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then -transamount else 0 end) as total, sum(case when status = 'R' then -transamount else 0 end) as reconciled
    from checkingaccount_v1
    where transcode in ('Transfer')
    group by accountid, transcode

    union

    select toaccountid AS accountid, transcode, sum(case when status in ('R', 'F', 'D', '')  then totransamount else 0 end) as total, sum(case when status = 'R' then totransamount else 0 end) as reconciled
    from checkingaccount_v1
    where transcode in ('Transfer') and toaccountid <> -1
    group by toaccountid, transcode
)  t 
group by accountid 
) T1 ON ACCOUNTLIST_V1.ACCOUNTID=T1.ACCOUNTID 
LEFT OUTER JOIN CURRENCYFORMATS_V1 ON ACCOUNTLIST_V1.CURRENCYID=CURRENCYFORMATS_V1.CURRENCYID 
WHERE ACCOUNTLIST_V1.ACCOUNTTYPE IN ('Checking', 'Term', 'Credit Card', 'Investment')
