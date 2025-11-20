package com.money.manager.ex.database;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.money.manager.ex.R;

/**
 * Utility class to manage password input using an AlertDialog.
 * Provides a standardized and reusable way to ask the user for a password.
 */
public class Password {

    /**
     * Callback interface to handle the outcome of the password entry dialog.
     */
    public interface PasswordCallback {
        /**
         * Called when the user enters a valid password and presses OK.
         * @param password The password entered by the user.
         */
        void onPasswordEntered(@NonNull String password);

        /**
         * Called when the user cancels the password entry operation.
         */
        void onPasswordCancelled();
    }

    /**
     * Shows an AlertDialog to ask the user to enter a password.
     * The dialog is not dismissible by tapping outside its boundaries.
     *
     * @param context         The context (preferably an Activity) to build the dialog.
     * @param callback        The callback to handle the result (success or cancellation).
     */
    public static void ask(@NonNull Context context, @NonNull final PasswordCallback callback) {
        // Checks if the context is still valid to prevent crashes.
        if (context instanceof Activity && (((Activity) context).isFinishing() || ((Activity) context).isDestroyed())) {
            // If the activity is finishing, do not show the dialog.
            // Notify the callback that the operation has been cancelled.
            callback.onPasswordCancelled();
            return;
        }

        // Creates an EditText for the password input.
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint(R.string.enter_password); // Uses a string resource for the hint

        // Adds some margin to the EditText for better appearance.
        // AlertDialog does not have default margins for custom views.
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        int margin = (int) (20 * context.getResources().getDisplayMetrics().density); // 20dp
        params.leftMargin = margin;
        params.rightMargin = margin;
        input.setLayoutParams(params);
        container.addView(input);

        // Builds the AlertDialog.
        new AlertDialog.Builder(context)
                .setTitle(R.string.enter_password)
                .setMessage(R.string.password_hint)
                .setView(container) // Adds the container with the EditText.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String password = input.getText().toString();
                        // Checks that the password is not empty before notifying success.
                        if (!TextUtils.isEmpty(password)) {
                            callback.onPasswordEntered(password);
                        } else {
                            // If the password is empty, treat it as a cancellation.
                            callback.onPasswordCancelled();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // The user pressed "Cancel".
                        dialog.cancel();
                        callback.onPasswordCancelled();
                    }
                })
                .setCancelable(false) // Prevents the user from closing the dialog by clicking outside.
                .show();
    }
}