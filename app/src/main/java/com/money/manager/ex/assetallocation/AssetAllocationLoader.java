/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex.assetallocation;

import android.content.Context;
import android.net.Uri;

import com.money.manager.ex.datalayer.AssetClassRepository;
import com.money.manager.ex.datalayer.AssetClassStockRepository;
import com.money.manager.ex.domainmodel.AssetClass;
import com.money.manager.ex.servicelayer.AssetAllocationService;

import androidx.loader.content.AsyncTaskLoader;

/**
 * Custom loader for Asset Allocation. Used to plug into the infrastructure and get
 * notifications on updated/deleted/created objects.
 * Ref: http://www.androiddesignpatterns.com/2012/08/implementing-loaders.html
 */
public class AssetAllocationLoader
    extends AsyncTaskLoader<AssetClass> {

    public AssetAllocationLoader(Context context) {
        super(context);
    }

    private AssetClass mData;

    /****************************************************/
    /** (1) A task that performs the asynchronous load **/
    /****************************************************/

    @Override
    public AssetClass loadInBackground() {
        AssetAllocationService service = new AssetAllocationService(getContext());
        AssetClass result = service.loadAssetAllocation();
        return result;
    }

    /********************************************************/
    /** (2) Deliver the results to the registered listener **/
    /********************************************************/

    @Override
    public void deliverResult(AssetClass data) {
        if (isReset()) {
            // The Loader has been reset; ignore the result and invalidate the data.
            releaseResources(data);
            return;
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        AssetClass oldData = mData;
        mData = data;

        if (isStarted()) {
            // If the Loader is in a started state, deliver the results to the
            // client. The superclass method does this for us.
            super.deliverResult(data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != data) {
            releaseResources(oldData);
        }
    }

    /*********************************************************/
    /** (3) Implement the Loader’s state-dependent behavior **/
    /*********************************************************/

    @Override
    protected void onStartLoading() {
        if (mData != null) {
            // Deliver any previously loaded data immediately.
            deliverResult(mData);
        }

        // Begin monitoring the underlying data source.
        if (mObserver == null) {
            mObserver = new AssetAllocationContentObserver(null, this);
            // register the observer
            registerObserver();
        }

        if (takeContentChanged() || mData == null) {
            // When the observer detects a change, it should call onContentChanged()
            // on the Loader, which will cause the next call to takeContentChanged()
            // to return true. If this is ever the case (or if the current data is
            // null), we force a new load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // The Loader is in a stopped state, so we should attempt to cancel the
        // current load (if there is one).
        cancelLoad();

        // Note that we leave the observer as is. Loaders in a stopped state
        // should still monitor the data source for changes so that the Loader
        // will know to force a new load if it is ever started again.
    }

    @Override
    protected void onReset() {
        // Ensure the loader has been stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'mData'.
        if (mData != null) {
            releaseResources(mData);
            mData = null;
        }

        // The Loader is being reset, so we should stop monitoring for changes.
        if (mObserver != null) {
            // unregister the observer
            unregisterObserver();

            mObserver = null;
        }
    }

    @Override
    public void onCanceled(AssetClass data) {
        // Attempt to cancel the current asynchronous load.
        super.onCanceled(data);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(data);
    }

    public AssetAllocationContentObserver getObserver() {
        return mObserver;
    }

    private void releaseResources(AssetClass data) {
        // For a simple List, there is nothing to do. For something like a Cursor, we
        // would close it in this method. All resources associated with the Loader
        // should be released here.
    }

    /*********************************************************************/
    /** (4) Observer which receives notifications when the data changes **/
    /*********************************************************************/

    // NOTE: Implementing an observer is outside the scope of this post (this example
    // uses a made-up "SampleObserver" to illustrate when/where the observer should
    // be initialized).

    // The observer could be anything so long as it is able to detect content changes
    // and report them to the loader with a call to onContentChanged(). For example,
    // if you were writing a Loader which loads a list of all installed applications
    // on the device, the observer could be a BroadcastReceiver that listens for the
    // ACTION_PACKAGE_ADDED intent, and calls onContentChanged() on the particular
    // Loader whenever the receiver detects that a new application has been installed.
    // Please don’t hesitate to leave a comment if you still find this confusing! :)
    private AssetAllocationContentObserver mObserver;

    private void registerObserver() {
        Uri assetClassUri = new AssetClassRepository(getContext()).getUri();
        Uri linkUri = new AssetClassStockRepository(getContext()).getUri();

        getContext().getContentResolver().registerContentObserver(assetClassUri, true, getObserver());
        getContext().getContentResolver().registerContentObserver(linkUri, true, getObserver());
    }

    private void unregisterObserver() {
        getContext().getContentResolver().unregisterContentObserver(getObserver());
    }
}
