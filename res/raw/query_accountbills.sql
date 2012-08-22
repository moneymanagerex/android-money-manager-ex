SELECT ACCOUNTLIST_V1.ACCOUNTID AS _id, ACCOUNTNAME, (INITIALBAL + T1.TOTAL) AS TOTAL
FROM ACCOUNTLIST_V1, (
select accountid, ROUND(SUM(total), 2) as total
from (
-- prelievo
select accountid, transcode, sum(-transamount) as total
from checkingaccount_v1
where status in ('R', '') and transcode in ('Withdrawal')
group by accountid, transcode

union

-- deposit
select accountid, transcode, sum(transamount) as total
from checkingaccount_v1
where status in ('R', '') and transcode in ('Deposit')
group by accountid, transcode

union

-- trasferiti
select accountid, transcode, sum(-transamount) as total
from checkingaccount_v1
where status in ('R', '') and transcode in ('Transfer')
group by accountid, transcode

union

-- ricevuti
select toaccountid AS accountid, transcode, sum(totransamount) as total
from checkingaccount_v1
where status in ('R', '') and transcode in ('Transfer') and toaccountid <> -1
group by toaccountid, transcode
)  t
group by accountid
) T1
WHERE ACCOUNTLIST_V1.ACCOUNTID=T1.ACCOUNTID
ORDER BY ACCOUNTNAME