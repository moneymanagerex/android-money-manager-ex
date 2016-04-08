package com.money.manager.ex.transactions.events;

import com.money.manager.ex.database.ITransactionEntity;

/**
 * Split item removed by the user.
 * Called from the individual split item fragment.
 */
public class SplitItemRemovedEvent {
    public SplitItemRemovedEvent(ITransactionEntity entity) {
        this.entity = entity;
    }

    public ITransactionEntity entity;
}
