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

package com.money.manager.ex.transactions;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.money.manager.ex.transactions.events.DialogNegativeClickedEvent;
import com.money.manager.ex.transactions.events.DialogPositiveClickedEvent;

import org.greenrobot.eventbus.EventBus;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

/**
 */
public class YesNoDialog
    extends DialogFragment {

    public static final String PURPOSE_DELETE_SPLITS_WHEN_SWITCHING_TO_TRANSFER = "delete-splits";

    public YesNoDialog() {

    }

    /**
     * Here we store the identifier in which context the binaryDialog is used.
     * Since this binaryDialog can be used for any binary outcome, there needs to be a way
     * to distinguish which one it is handling.
     * This is used in the caller to distinguish which action to take in case there are
     * multiple instances of yes-no binaryDialog.
     * If there is only one then it does not need to be used.
     */
    private String mPurpose;

    public String getPurpose() {
        return mPurpose;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        String title = args.getString("title", "");
        String message = args.getString("message", "");
        this.mPurpose = args.getString("purpose", "");

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        EventBus.getDefault().post(new DialogPositiveClickedEvent());
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        EventBus.getDefault().post(new DialogNegativeClickedEvent());
                    }
                })
                .create();
    }
}
