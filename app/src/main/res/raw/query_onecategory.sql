WITH RECURSIVE categories(CATEGID, CATEGNAME, PARENTID, PARENTNAME, BASENAME, FULLCATID, ACTIVE, LEVEL) AS
(SELECT a.categid, a.categname, a.parentid, "" as parentname, a.categname as basename, ":" || a.CATEGID || ":" as fullcatid, a.ACTIVE, 1 FROM category_v1 a WHERE parentid = '-1'
UNION ALL
SELECT c.categid, r.categname || ':' || c.categname, c.parentid, r.categname as parentname, c.categname as basename, r.fullcatid || c.CATEGID || ":" as fullcatid, c.ACTIVE, r.LEVEL + 1
FROM categories r, category_v1 c
WHERE r.categid = c.parentid)
 SELECT categories.CATEGID as _id, categories.* FROM categories