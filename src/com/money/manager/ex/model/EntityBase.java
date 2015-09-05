package com.money.manager.ex.model;

import android.database.Cursor;

/**
 * Base for the model entities. Keeps a reference to a cursor that contains the underlying data.
 * Created by Alen on 5/09/2015.
 */
public class EntityBase {

    /**
     * Default constructor.
     */
    protected EntityBase() {

    }

    protected EntityBase(Cursor c) {
        mCursor = c;
    }

    /**
     * Contains the pointer to the actual data.
     */
    protected Cursor mCursor;
}
