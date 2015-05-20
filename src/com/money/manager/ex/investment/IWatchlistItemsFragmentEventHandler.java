package com.money.manager.ex.investment;

/**
 * Interface is to be implemented by the parent of WatchlistItemsFragment
 */
public interface IWatchlistItemsFragmentEventHandler {
    void onPriceUpdateRequested(String symbol);
}
