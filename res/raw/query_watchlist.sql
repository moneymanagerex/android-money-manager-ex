-- Retrieve watchlist symbols with latest prices
select stockid, s.symbol, h.date, h.value
from stock_v1 s
	left outer join stockhistory_v1 h on h.symbol = s.symbol
group by s.symbol
where s.heldat = ?