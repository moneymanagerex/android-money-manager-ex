--query_budgets.sql -- DA VERIFICARE CON SORGENTE ORIGINALE
WITH RECURSIVE categories(categid, categname, parentid) AS
    (SELECT a.categid, a.categname, a.parentid FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.parentid
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
-- Get the Budgets for display
select b.*,
	c.CategName,
	null as SubCategName
from budgettable_v1 b
    left outer join categories c on b.categid = c.categid
