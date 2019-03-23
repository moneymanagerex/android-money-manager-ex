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
package com.money.manager.ex.settings;

import android.content.Intent;
import android.os.Bundle;

import com.money.manager.ex.sync.SyncPreferenceFragment;

import java.util.Set;

import timber.log.Timber;

public class SyncPreferencesActivity
    extends BaseSettingsFragmentActivity {

    private static final String BROWSABLE = "android.intent.category.BROWSABLE";

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);

        setSettingFragment(new SyncPreferenceFragment());
    }

    /**
     * Handle authentication redirect from an external browser. Handles Google authentication.
     * Ref: https://documentation.cloudrail.com/android/android/Usage#external-authentication
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        Set<String> categories = intent.getCategories();

        if(categories != null && categories.contains(BROWSABLE)) {
            // Here we pass the response to the SDK which will automatically
            // complete the authentication process
            Timber.d("setting OAuth authentication response from Google");
//            CloudRail.setAuthenticationResponse(intent);
        }

        super.onNewIntent(intent);
    }
}
