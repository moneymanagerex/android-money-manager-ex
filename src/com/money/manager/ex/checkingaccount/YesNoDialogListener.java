package com.money.manager.ex.checkingaccount;

import android.support.v4.app.DialogFragment;

/**
 * Used to attach listener to binary YesNoDialog.
 *
 * The activity that creates an instance of this dialog fragment must
 * implement this interface in order to receive event callbacks.
 * Each method passes the DialogFragment in case the host needs to query it.
 */
public interface YesNoDialogListener {
    void onDialogPositiveClick(DialogFragment dialog);
    void onDialogNegativeClick(DialogFragment dialog);
}
