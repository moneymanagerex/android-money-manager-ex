package com.money.manager.ex.utils;

import android.app.ProgressDialog;

/**
 * Common dialog utility functions.
 */
public class DialogUtils {
    public static void closeProgressDialog(ProgressDialog progressDialog) {
        progressDialog.hide();
        progressDialog.dismiss();
    }

}
