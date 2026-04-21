-- Migration for Robust Offline-First Sync via PocketBase

-- Add updated_at, is_deleted, and is_dirty columns to synchronizable tables
-- For now, we'll start with the main transactions table.
-- In a full implementation, this should be applied to all synchronizable tables.

-- checkingaccount_v1 already has LASTUPDATEDTIME and DELETEDTIME.
-- We'll add IS_DIRTY to track local changes pending upload.
ALTER TABLE CHECKINGACCOUNT_V1 ADD COLUMN IS_DIRTY INTEGER DEFAULT 0;

-- Ensure other tables have these columns if they don't already.
-- Account List
ALTER TABLE ACCOUNTLIST_V1 ADD COLUMN updated_at TEXT;
ALTER TABLE ACCOUNTLIST_V1 ADD COLUMN is_deleted INTEGER DEFAULT 0;
ALTER TABLE ACCOUNTLIST_V1 ADD COLUMN is_dirty INTEGER DEFAULT 0;

-- Category
ALTER TABLE CATEGORY_V1 ADD COLUMN updated_at TEXT;
ALTER TABLE CATEGORY_V1 ADD COLUMN is_deleted INTEGER DEFAULT 0;
ALTER TABLE CATEGORY_V1 ADD COLUMN is_dirty INTEGER DEFAULT 0;

-- Payee
ALTER TABLE PAYEE_V1 ADD COLUMN updated_at TEXT;
ALTER TABLE PAYEE_V1 ADD COLUMN is_deleted INTEGER DEFAULT 0;
ALTER TABLE PAYEE_V1 ADD COLUMN is_dirty INTEGER DEFAULT 0;

-- Set initial updated_at for existing records
UPDATE CHECKINGACCOUNT_V1 SET LASTUPDATEDTIME = strftime('%Y-%m-%dT%H:%M:%SZ', 'now') WHERE LASTUPDATEDTIME IS NULL;
UPDATE ACCOUNTLIST_V1 SET updated_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now') WHERE updated_at IS NULL;
UPDATE CATEGORY_V1 SET updated_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now') WHERE updated_at IS NULL;
UPDATE PAYEE_V1 SET updated_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now') WHERE updated_at IS NULL;
