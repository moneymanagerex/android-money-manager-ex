SELECT SUB2.Year, SUB2.Month, SUM(SUB2.i) AS Income, SUM(SUB2.e) AS Expenses, SUM(SUB2.t) AS Transfers
FROM (
    select sub1.month, sub1.year,
    case when lower(sub1.transactiontype)='deposit' then sub1.total else 0 end as i,
    case when lower(sub1.transactiontype)='withdrawal' then sub1.total else 0 end as e,
    case when lower(sub1.transactiontype)='transfer' then sub1.total else 0 end as t
    from (
        select mobiledata.month, mobiledata.year, mobiledata.transactiontype, sum(mobiledata.AmountBaseConvRate) as total
        from %%mobiledata%%
        where not(mobiledata.status = 'V')
            and lower(mobiledata.transactiontype) in ('deposit', 'withdrawal')
            and mobiledata.ID not in (
                select tl.CHECKINGACCOUNTID
                from TRANSLINK_V1 tl
                join CHECKINGACCOUNT_V1 ca on ca.TRANSID = tl.CHECKINGACCOUNTID
                where lower(tl.LINKTYPE) = 'stock'
                    and ca.TOACCOUNTID is not null
                    and ca.TOACCOUNTID <> -1
                union
                select si.CHECKINGACCOUNTID
                from SHAREINFO_V1 si
                join CHECKINGACCOUNT_V1 ca on ca.TRANSID = si.CHECKINGACCOUNTID
                where ca.TOACCOUNTID is not null
                    and ca.TOACCOUNTID <> -1
            )
        group by month, year, transactiontype
        ) sub1
    ) SUB2
GROUP BY SUB2.Year, SUB2.Month

UNION ALL
-- 	 The total for the year
SELECT SUB2.Year, 99 AS Month, SUM(SUB2.i) AS Income, SUM(SUB2.e) AS Expenses, SUM(SUB2.t) AS Transfers
FROM (
	select sub1.month, sub1.year,
	case when lower(sub1.transactiontype)='deposit' then sub1.total else 0 end as i,
	case when lower(sub1.transactiontype)='withdrawal' then sub1.total else 0 end as e,
	case when lower(sub1.transactiontype)='transfer' then sub1.total else 0 end as t
	from (
		select mobiledata.month, mobiledata.year, mobiledata.transactiontype, sum(mobiledata.AmountBaseConvRate) as total
		from %%mobiledata%%
    where not(mobiledata.status = 'V')
            and lower(mobiledata.transactiontype) in ('deposit', 'withdrawal')
            and mobiledata.ID not in (
                        select tl.CHECKINGACCOUNTID
                        from TRANSLINK_V1 tl
                        join CHECKINGACCOUNT_V1 ca on ca.TRANSID = tl.CHECKINGACCOUNTID
                        where lower(tl.LINKTYPE) = 'stock'
                            and ca.TOACCOUNTID is not null
                            and ca.TOACCOUNTID <> -1
                        union
                        select si.CHECKINGACCOUNTID
                        from SHAREINFO_V1 si
                        join CHECKINGACCOUNT_V1 ca on ca.TRANSID = si.CHECKINGACCOUNTID
                        where ca.TOACCOUNTID is not null
                            and ca.TOACCOUNTID <> -1
                    )
    group by month, year, transactiontype
    ) sub1
) SUB2
GROUP BY SUB2.Year
