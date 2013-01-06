SELECT SUB1.CATEGID, SUB1.SUBCATEGID, SUB1.CATEGORY, SUB1.SUBCATEGORY, SUM(TOTAL) AS TOTALCATEGORY
FROM (
select mobiledata.date, mobiledata.year, mobiledata.month, mobiledata.day, mobiledata.categid, mobiledata.subcategid, mobiledata.category, mobiledata.subcategory, sum(mobiledata.amount) as total
from mobiledata
where not(mobiledata.status='V')
group by mobiledata.date, mobiledata.year, mobiledata.month, mobiledata.day, mobiledata.categid, mobiledata.subcategid, mobiledata.category, mobiledata.subcategory
) SUB1
GROUP BY SUB1.CATEGID, SUB1.SUBCATEGID, SUB1.CATEGORY, SUB1.SUBCATEGORY