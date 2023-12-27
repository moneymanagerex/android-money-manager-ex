-- Get the Budgets for display
select b.*,
	c.CategName,
	sc.CategName as SubCategName
from budgettable_v1 b
    left outer join Category_v1 c on b.categid = c.categid
    left outer join Category_v1 sc on sc.categid = c.parentid
