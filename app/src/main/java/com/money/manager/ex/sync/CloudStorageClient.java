/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.sync;

import android.content.Context;

import com.cloudrail.si.exceptions.ParseException;
import com.cloudrail.si.interfaces.CloudStorage;
import com.cloudrail.si.services.Box;
import com.cloudrail.si.services.Dropbox;
import com.cloudrail.si.services.GoogleDrive;
import com.cloudrail.si.services.OneDrive;
import com.cloudrail.si.types.CloudMetaData;
import com.money.manager.ex.R;
import com.money.manager.ex.settings.SyncPreferences;
import com.money.manager.ex.utils.NetworkUtils;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Single;
import rx.SingleSubscriber;
import rx.functions.Action0;
import timber.log.Timber;

/**
 * Direct interface to the current cloud provider.
 * Extracted from the Sync Manager.
 */
class CloudStorageClient {

    public CloudStorageClient(Context context) {
        mContext = context;

        // Do not initialize providers if the network is not present.
        NetworkUtils network = new NetworkUtils(getContext());
        if (!network.isOnline()) return;

        createProviders();
        restoreProviderCache();

        // Use current provider.
        String providerCode = getPreferences().loadPreference(R.string.pref_sync_provider, CloudStorageProviderEnum.DROPBOX.name());
        CloudStorageProviderEnum provider = CloudStorageProviderEnum.DROPBOX;
        if (CloudStorageProviderEnum.contains(providerCode)) {
            provider = CloudStorageProviderEnum.valueOf(providerCode);
        }
        setProvider(provider);
    }

    private final AtomicReference<CloudStorage> dropbox = new AtomicReference<>();
    private final AtomicReference<CloudStorage> box = new AtomicReference<>();
    private final AtomicReference<CloudStorage> googledrive = new AtomicReference<>();
    private final AtomicReference<CloudStorage> onedrive = new AtomicReference<>();

    private Context mContext;
    private AtomicReference<CloudStorage> currentProvider;
    private SyncPreferences mPreferences;

    public Context getContext() {
        return mContext;
    }

    public Single<List<CloudMetaData>> getContents(final String folder) {
        return Observable.fromCallable(new Callable<List<CloudMetaData>>() {
            @Override
            public List<CloudMetaData> call() throws Exception {
                return getProvider().getChildren(folder);
            }
        })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        // save any renewed tokens
                        cacheCredentials();
                    }
                })
                .toSingle();
    }

    public InputStream download(String s) {
        return getProvider().download(s);
    }

    public CloudMetaData loadMetadata(final String remotePath) {
        final CloudMetaData[] result = {null};

        Single.fromCallable(new Callable<CloudMetaData>() {
            @Override
            public CloudMetaData call() throws Exception {
                return getProvider().getMetadata(remotePath);
            }
        })
                .retry(1)
                .subscribe(new SingleSubscriber<CloudMetaData>() {
                    @Override
                    public void onSuccess(CloudMetaData value) {
                        // save any renewed tokens
                        CloudStorageClient.this.cacheCredentials();

                        result[0] = value;
                    }

                    @Override
                    public void onError(Throwable error) {
                        // todo handle DNS exceptions by just showing a message?
                        //if (error instanceof RuntimeException && error.getMessage().equals("Unable to resolve host \"api.dropboxapi.com\": No address associated with hostname"))
                        // Unable to resolve host "www.googleapis.com": No address associated with hostname

                        Timber.e(error, "fetching remote metadata");
                    }
                });

        // .toBlocking().value()

        return result[0];
    }

    public Single<Void> login() {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getProvider().login();
                return null;
            }
        })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        // cache credentials after login.
                        cacheCredentials();
                    }
                })
                .toSingle();
    }

    public Single<Void> logout() {
        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                getProvider().logout();
                return null;
            }
        })
                .toSingle();
    }

    public void setProvider(CloudStorageProviderEnum provider) {
        // Sync provider mapping
        switch (provider) {
            case DROPBOX:
                currentProvider = dropbox;
                break;
            case ONEDRIVE:
                // OneDrive
                currentProvider = onedrive;
                break;
            case GOOGLEDRIVE:
                // Google Drive
                currentProvider = googledrive;
                break;
            case BOX:
                // Box
                currentProvider = box;
                break;
            default:
                // default provider
                currentProvider = dropbox;
                break;
        }
    }

    public void cacheCredentials() {
        if (dropbox.get() != null) {
            getPreferences().set(R.string.pref_dropbox_persistent, dropbox.get().saveAsString());
        }
        if (box.get() != null) {
            getPreferences().set(R.string.pref_onedrive_persistent, box.get().saveAsString());
        }
        if (googledrive.get() != null) {
            getPreferences().set(R.string.pref_gdrive_persistent, googledrive.get().saveAsString());
        }
        if (onedrive.get() != null) {
            getPreferences().set(R.string.pref_box_persistent, onedrive.get().saveAsString());
        }
    }

    public void upload(String s, InputStream inputStream, long l, boolean b) {
        getProvider().upload(s, inputStream, l, b);
    }

    /*
        Private
     */

    protected void createProviders() {
        try {
            dropbox.set(new Dropbox(getContext(), "6328lyguu3wwii6", "oa7k0ju20qss11l"));
            onedrive.set(new OneDrive(getContext(), "b76e0230-4f4e-4bff-9976-fd660cdebc4a", "fmAOPrAuq6a5hXzY1v7qcDn"));
            googledrive.set(new GoogleDrive(getContext(), "843259487958-p65svijbdvj1knh5ove1ksp0hlnufli8.apps.googleusercontent.com", "cpU0rnBiMW9lQaYfaoW1dwLU"));
            box.set(new Box(getContext(), "95f7air3i2ed19r28hi31vwtta4wgz1p", "i6j0NLd3G6Ui9FpZyuQfiLK8jLs4YZRM"));
        } catch (Exception e) {
            Timber.e(e, "creating cloud providers");
        }
    }

    private SyncPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = new SyncPreferences(getContext());
        }
        return mPreferences;
    }

    private CloudStorage getProvider() {
        return currentProvider.get();
    }

    private void restoreProviderCache() {
        try {
            String persistent = getPreferences().loadPreference(R.string.pref_dropbox_persistent, null);
            if (persistent != null) dropbox.get().loadAsString(persistent);

            persistent = getPreferences().loadPreference(R.string.pref_box_persistent, null);
            if (persistent != null) box.get().loadAsString(persistent);

            persistent = getPreferences().loadPreference(R.string.pref_gdrive_persistent, null);
            if (persistent != null) googledrive.get().loadAsString(persistent);

            persistent = getPreferences().loadPreference(R.string.pref_onedrive_persistent, null);
            if (persistent != null) onedrive.get().loadAsString(persistent);
        } catch (Exception e) {
            if (e instanceof ParseException) {
                Timber.w(e.getMessage());
            } else {
                Timber.e(e, "restoring providers from cache");
            }
        }
    }

}
