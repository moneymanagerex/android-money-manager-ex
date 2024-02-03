-- one_category_list.sql
-- set are required from Codacy
SET NOCOUNT ON
SET QUOTED_IDENTIFIER ON
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED
SET ANSI_NULLS ON
WITH RECURSIVE categories(categid, categname, catbasename, parentid, parentcategname, active ) AS
    (SELECT a.categid, a.categname, a.categname AS catbasename, a.parentid, NULL AS parentcategname, a.active
	    FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.categname AS catbasename, c.parentid, r.categname, c.active AS parentcategname
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
SELECT * FROM categories
-- where active = 1
ORDER BY categname