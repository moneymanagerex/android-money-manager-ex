WITH
	RECURSIVE categories(CATEGID, CATEGNAME, PARENTID, PARENTNAME, BASENAME, FULLCATID, ACTIVE, LEVEL) AS
	(SELECT a.categid, a.categname, a.parentid, "" as parentname, a.categname as basename, ":" || a.CATEGID || ":" as fullcatid, ifnull( a.ACTIVE, 0) , 1 FROM category_v1 a WHERE parentid = '-1'
	UNION ALL
	SELECT c.categid, r.categname || ':' || c.categname, c.parentid, r.categname as parentname, c.categname as basename, r.fullcatid || c.CATEGID || ":" as fullcatid, ifnull(c.ACTIVE,0), r.LEVEL + 1
	FROM categories r, category_v1 c
	WHERE r.categid = c.parentid)
,
  children( id, child) as (
	SELECT PARENTID as ID, COUNT( * ) as child
	FROM CATEGORY_V1
	GROUP BY PARENTID)

 SELECT categories.CATEGID as _id,
        categories.* ,
        ifnull( children.child, 0 ) as CHILDRENCOUNT
   FROM categories
   LEFT JOIN children on children.ID = categories.CATEGID