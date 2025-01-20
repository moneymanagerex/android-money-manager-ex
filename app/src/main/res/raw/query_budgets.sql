-- Get the Budgets for display
--select b.*,
--	c.CategName,
--	sc.CategName as SubCategName
--from budgettable_v1 b
--    left outer join Category_v1 c on b.categid = c.categid
--    left outer join Category_v1 sc on sc.categid = c.parentid

-- Get the Budgets for display
WITH RECURSIVE categories(categid, categname, catshortname, parentid, parentcategname ) AS
    (SELECT a.categid, a.categname, a.categname AS catshortname, a.parentid, NULL AS parentcategname FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.CATEGNAME AS catshortname, c.parentid, r.categname AS parentcategname
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
select b.*,
	c.categname
from budgettable_v1 b
    left outer join categories c on b.categid = c.categid
