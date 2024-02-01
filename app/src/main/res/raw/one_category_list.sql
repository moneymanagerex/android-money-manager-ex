-- one_category_list.sql
WITH RECURSIVE categories(categid, categname, catbasename, parentid, parentcategname, active ) AS
    (SELECT a.categid, a.categname, a.categname as catbasename, a.parentid, null as parentcategname, a.ACTIVE
	    FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.CATEGNAME as catbasename, c.parentid, r.categname, c.ACTIVE as parentcategname
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
select *  from categories
-- where active = 1
order by categname