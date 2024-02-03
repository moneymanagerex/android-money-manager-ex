-- one_category_list.sql
WITH RECURSIVE categories(categid, categname, catbasename, parentid, parentcategname, active ) AS
    (SELECT a.categid, a.categname, a.categname as catbasename, a.parentid, NULL as parentcategname, a.active
	    FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.categname as catbasename, c.parentid, r.categname, c.active as parentcategname
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
SELECT * FROM categories
-- where active = 1
ORDER BY categname