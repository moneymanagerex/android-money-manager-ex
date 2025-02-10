-- realign missing category
-- set as inactive with <recovery> info
UPDATE CATEGORY_V1
  set active = 0,
      PARENTID = -1,
      CATEGNAME = CATEGNAME || " [recovered]"
  where CATEGID = PARENTID;

-- from master repo
-- db tidy, fix corrupt indices
REINDEX;

-- To alleviate future issues we are normalizing the TRANSDATE column
UPDATE CHECKINGACCOUNT_V1 SET TRANSDATE = TRANSDATE || 'T00:00:00' WHERE LENGTH(TRANSDATE)=10;