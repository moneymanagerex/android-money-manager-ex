--query_budgets.sql -- DA VERIFICARE CON SORGENTE ORIGINALE
WITH RECURSIVE categories(categid, categname, parentid) AS
    (SELECT a.categid, a.categname, a.parentid FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.parentid
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
-- Get the Budgets for display
SELECT b.*,
	c.CategName,
	NULL as SubCategName
FROM budgettable_v1 b
    LEFT OUTER JOIN categories c ON b.categid = c.categid
