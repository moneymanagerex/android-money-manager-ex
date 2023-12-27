-- db tidy -- remove blank records caused by https://github.com/moneymanagerex/moneymanagerex/issues/5630
DELETE FROM CHECKINGACCOUNT_V1 WHERE ACCOUNTID = '-1';

-- Payee Matching
-- https://github.com/moneymanagerex/moneymanagerex/issues/3148
ALTER TABLE PAYEE_V1 ADD COLUMN 'PATTERN' TEXT DEFAULT '';
