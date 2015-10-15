package com.money.manager.ex.assetallocation;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.Loader;
import android.util.Log;

import com.money.manager.ex.domainmodel.AssetClass;

/**
 * Content observer that glues data change notifications and Asset Allocation Loader.
 */
public class AssetAllocationContentObserver
    extends ContentObserver {

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public AssetAllocationContentObserver(Handler handler, Loader<AssetClass> loader) {
        super(handler);

        this.loader = loader;
    }

    private Loader<AssetClass> loader;

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        // do s.th.
        // depending on the handler you might be on the UI
        // thread, so be cautious!

//        Log.d("observer", "change detected");
        // notify Loader#onContentChanged() somehow...
        this.loader.onContentChanged();
    }
}
