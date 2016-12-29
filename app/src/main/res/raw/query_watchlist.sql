-- Retrieve watchlist symbols with latest prices
select s.stockid, s.symbol, s.stockname, h.date, h.value
from stock_v1 s
	left outer join stockhistory_v1 h on h.symbol = s.symbol
where s.heldat = ?
group by s.symbol