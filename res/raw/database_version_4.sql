-- Asset Classes
CREATE TABLE ASSETCLASS_V1 (
    ID integer primary key,
    NAME TEXT COLLATE NOCASE NOT NULL,
    ALLOCATION numeric
)

-- Asset Class / Stock link table
CREATE TABLE ASSETCLASS_STOCK_V1 (
    ID integer primary key,
    ASSETCLASSID integer UNIQUE,
    STOCKID integer UNIQUE
)
