package com.money.manager.ex.utils;

import android.app.ProgressDialog;
import android.util.Log;

import com.money.manager.ex.core.ExceptionHandler;

/**
 * Common dialog utility functions.
 */
public class DialogUtils {
    public static void closeProgressDialog(ProgressDialog progressDialog) {
        try {
            progressDialog.hide();
            progressDialog.dismiss();
        } catch (Exception ex) {
            //ExceptionHandler handler = new ExceptionHandler(getapp)
//            Log.e("Dialog Utils", )
            ex.printStackTrace();
        }
    }

}
