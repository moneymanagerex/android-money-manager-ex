-- query_categorysubcategory.sql
-- based on one_category_list
-- set are required from Codacy.
SET NOCOUNT ON
SET QUOTED_IDENTIFIER ON
SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED
SET ANSI_NULLS ON
WITH RECURSIVE categories(categid, categname, catbasename, parentid, parentcategname, active ) AS
    (SELECT a.categid, a.categname, a.categname as catbasename, a.parentid, NULL AS parentcategname, a.ACTIVE
	    FROM category_v1 a WHERE parentid = '-1'
        UNION ALL
     SELECT c.categid, r.categname || ':' || c.categname, c.CATEGNAME AS catbasename, c.parentid, r.categname, c.ACTIVE AS parentcategname
     FROM categories r, category_v1 c
	 WHERE r.categid = c.parentid
	 )
SELECT categid AS _id,
       CATEGID AS CATEGID,
	   categname AS CATEGNAME,
	   -1 AS SUBCATEGID,
       '' AS SUBCATEGNAME,
--	   categname as CATEGSUBNAME,
	   catbasename AS CATEGBASENAME,
	   parentid AS PARENTID,
	   ifnull( parentcategname, '') AS PARENTCATEGNAME
FROM categories
-- where active = 1
ORDER BY categname
