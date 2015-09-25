package com.money.manager.ex.investment;

import java.util.List;

/**
 * Interface for security price updater functionality.
 * Any module that downloads prices should implement this so that it is easy to replace
 * different implementations of price providers.
 */
public interface ISecurityPriceUpdater {
    /**
     * Download prices for all the securities and update the values.
     * Store the values in the history table.
     */
    void downloadPrices(List<String> symbols);
}
