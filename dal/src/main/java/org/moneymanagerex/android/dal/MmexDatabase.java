package org.moneymanagerex.android.dal;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Database
 * Ref: https://github.com/Raizlabs/DBFlow/blob/master/usage/GettingStarted.md
 */
@Database(name = MmexDatabase.NAME, version = MmexDatabase.VERSION)
public class MmexDatabase {

    public static final String NAME = "MmexDatabase";

    public static final int VERSION = 4;
}