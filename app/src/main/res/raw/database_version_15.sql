-- Will fail if v8-14 by recreating a column that exists in those versions
-- so only upgrade from v1-v7 possible. Need to downgrade other versions prior to upgrade 
alter table CURRENCYFORMATS_V1 add column CURRENCY_TYPE TEXT;
update CURRENCYFORMATS_V1 set CURRENCY_TYPE = 'Fiat';
update CURRENCYFORMATS_V1 set CURRENCY_TYPE = 'Crypto' where CURRENCY_SYMBOL NOT IN
('ADP', 'AED', 'AFA', 'AFN', 'ALK', 'ALL', 'AMD', 'ANG', 'AOA', 'AOK', 'AON', 'AOR', 'ARA', 
'ARP', 'ARS', 'ARY', 'ATS', 'AUD', 'AWG', 'AYM', 'AZM', 'AZN', 'BAD', ' BAM', 'BBD', 'BDT', 
'BEC', 'BEF', 'BEL', 'BGJ', 'BGK', 'BGL', 'BGN', 'BHD', 'BIF', 'BMD', 'BND', 'BOB', 'BOP', 
'BRB', 'BRC', 'BRE', 'BRL', 'BRN', 'BRR', 'BSD', ' BTN', 'BUK', 'BWP', 'BYB', 'BYN', 'BYR', 
'BZD', 'CAD', 'CDF', 'CHC', 'CHF', 'CLP', 'CNY', 'COP', 'CRC', 'CSD', 'CSJ', 'CSK', 'CUC', 
'CUP', 'CVE', 'CYP', 'CZK', ' DDM', 'DEM', 'DJF', 'DKK', 'DOP', 'DZD', 'ECS', 'ECV', 'EEK', 
'EGP', 'ERN', 'ESA', 'ESB', 'ESP', 'ETB', 'EUR', 'FIM', 'FJD', 'FKP', 'FRF', 'GBP', 'GEK', 
'GEL', ' GHC', 'GHP', 'GHS', 'GIP', 'GMD', 'GNE', 'GNF', 'GNS', 'GQE', 'GRD', 'GTQ', 'GWE', 
'GWP', 'GYD', 'HKD', 'HNL', 'HRD', 'HRK', 'HTG', 'HUF', 'IDR', 'IEP', 'ILP', ' ILR', 'ILS', 
'INR', 'IQD', 'IRR', 'ISJ', 'ISK', 'ITL', 'JMD', 'JOD', 'JPY', 'KES', 'KGS', 'KHR', 'KMF', 
'KPW', 'KRW', 'KWD', 'KYD', 'KZT', 'LAJ', 'LAK', 'LBP', ' LKR', 'LRD', 'LSL', 'LSM', 'LTL', 
'LTT', 'LUC', 'LUF', 'LUL', 'LVL', 'LVR', 'LYD', 'MAD', 'MDL', 'MGA', 'MGF', 'MKD', 'MLF', 
'MMK', 'MNT', 'MOP', 'MRO', 'MRU', ' MTL', 'MTP', 'MUR', 'MVQ', 'MVR', 'MWK', 'MXN', 'MXP', 
'MYR', 'MZE', 'MZM', 'MZN', 'NAD', 'NGN', 'NIC', 'NIO', 'NLG', 'NOK', 'NPR', 'NZD', 'OMR', 
'PAB', 'PEH', ' PEI', 'PEN', 'PES', 'PGK', 'PHP', 'PKR', 'PLN', 'PLZ', 'PTE', 'PYG', 'QAR', 
'RHD', 'ROK', 'ROL', 'RON', 'RSD', 'RUB', 'RUR', 'RWF', 'SAR', 'SBD', 'SCR', 'SDD', ' SDG', 
'SDP', 'SEK', 'SGD', 'SHP', 'SIT', 'SKK', 'SLL', 'SOS', 'SRD', 'SRG', 'SSP', 'STD', 'STN', 
'SUR', 'SVC', 'SYP', 'SZL', 'THB', 'TJR', 'TJS', 'TMM', 'TMT', ' TND', 'TOP', 'TPE', 'TRL', 
'TRY', 'TTD', 'TWD', 'TZS', 'UAH', 'UAK', 'UGS', 'UGW', 'UGX', 'USD', 'USS', 'UYN', 'UYP', 
'UYU', 'UZS', 'VEB', 'VEF', 'VNC', 'VND', ' VUV', 'WST', 'XAF', 'XCD', 'XDR', 'XEU', 'XFO', 
'XOF', 'XPF', 'YDD', 'YER', 'YUD', 'YUM', 'YUN', 'ZAL', 'ZAR', 'ZMK', 'ZMW', 'ZRN', 'ZRZ', 
'ZWC', 'ZWD', 'ZWL', 'ZWN', 'ZWR', 'VUV', 'TND', 'SDG', 'LKR', 'BAM', 'BTN');

-- Setup date of account initial balance
-- https://github.com/moneymanagerex/moneymanagerex/issues/3554
alter table ACCOUNTLIST_V1 add column INITIALDATE text;
update ACCOUNTLIST_V1 SET INITIALDATE = ( select TRANSDATE from CHECKINGACCOUNT_V1 where 
    (ACCOUNTLIST_V1.ACCOUNTID = CHECKINGACCOUNT_V1.ACCOUNTID OR
    ACCOUNTLIST_V1.ACCOUNTID = CHECKINGACCOUNT_V1.TOACCOUNTID )
    order by TRANSDATE asc limit 1 );
update ACCOUNTLIST_V1 SET INITIALDATE = (select PURCHASEDATE from STOCK_V1 where
    (ACCOUNTLIST_V1.ACCOUNTID = STOCK_V1.HELDAT)
    order by PURCHASEDATE asc limit 1 )  where INITIALDATE is null;
update ACCOUNTLIST_V1 set INITIALDATE = date() where INITIALDATE is null;

alter table ASSETS_V1 add column ASSETSTATUS TEXT;
alter table ASSETS_V1 add column CURRENCYID integer;
alter table ASSETS_V1 add column VALUECHANGEMODE TEXT;
update ASSETS_V1 set ASSETSTATUS = 'Open', CURRENCYID = -1, VALUECHANGEMODE = 'Percentage';

alter table BUDGETSPLITTRANSACTIONS_V1 add column NOTES TEXT;
alter table SPLITTRANSACTIONS_V1 add column NOTES TEXT;

alter table BUDGETTABLE_V1 add column NOTES TEXT;
alter table BUDGETTABLE_V1 add column ACTIVE integer;
update BUDGETTABLE_V1 set ACTIVE = 1;

alter table CATEGORY_V1 add column ACTIVE integer;
alter table SUBCATEGORY_V1 add column ACTIVE integer;
update CATEGORY_V1 set ACTIVE = 1;
update SUBCATEGORY_V1 set ACTIVE = 1;

alter table PAYEE_V1 add column NUMBER TEXT;
alter table PAYEE_V1 add column WEBSITE TEXT;
alter table PAYEE_V1 add column NOTES TEXT;
alter table PAYEE_V1 add column ACTIVE integer;
update PAYEE_V1 set ACTIVE = 1;

alter table REPORT_V1 add column ACTIVE integer;
update REPORT_V1 set ACTIVE = 1;

-- Tidy-up: This table was in the schema but has been removed and should not exist
drop table if exists SPLITTRANSACTIONS_V2;

