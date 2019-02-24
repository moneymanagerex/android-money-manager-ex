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

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.money.manager.ex.R;

public class BehaviourSettingsActivity
    extends BaseSettingsFragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setSettingFragment(new BehaviourSettingsFragment());
    }

    //Author:- velmuruganc - Added for Issue : #1144 - Add automatic bank transaction updates
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        final BehaviourSettings settings = new BehaviourSettings(this);

        switch (requestCode)
        {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                {
                    Toast.makeText(this,
                            R.string.deny_receive_sms_error, Toast.LENGTH_LONG).show();

                    settings.setBankSmsTrans(false);

                }
                break;
        }

    }

}
