-- Get the Budgets for display
select b.*,
	c.CategName,
	sc.SubCategName
from budgettable_v1 b
    left outer join Category_v1 c on b.categid = c.categid
    left outer join Subcategory_v1 sc on sc.subcategid = b.subcategid
