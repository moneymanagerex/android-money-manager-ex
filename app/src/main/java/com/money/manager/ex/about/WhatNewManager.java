package com.money.manager.ex.about;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class WhatNewManager {
    private static final String TAG = "WhatNewManager";
    private static final String PREFS_NAME = "WhatNewPrefs"; // Name of the preferences file
    private static final String KEY_LAST_SEEN_VERSION = "last_seen_version"; // Key to save the last seen version

    private final Context context; // Application context

    /**
     * Constructor for the WhatNewManager class.
     *
     * @param context The application context.
     */
    public WhatNewManager(Context context) {
        this.context = context;
    }

    /**
     * Shows the "What's New" dialog if the current app version is
     * greater than the last version the user has seen.
     *
     * @param activity The Activity from which this method is called,
     * necessary to show the dialog.
     */
    public void showWhatsNewIfNeeded(Activity activity) {
        int lastSeenVersion = getLastSeenVersion(); // Retrieve the last seen version
        int currentVersion = getCurrentAppVersion(); // Retrieve the current app version
        if (lastSeenVersion == 0) {
            // we don't need to notify the user on new release since it was just downloaded
            // set this as last seen version
            saveLastSeenVersion(currentVersion);
            return ;
        }

        Timber.d( TAG + ": Last seen version: " + lastSeenVersion + ", current version: " + currentVersion);

        // If the current version is less than or equal to the last seen, do nothing.
        if (currentVersion <= lastSeenVersion) {
            Timber.d( "%s: no news to show", TAG);
            return;
        }

        // Retrieve changelog strings between the last seen version and the current one.
        List<String> changelogs = getChangelogStrings(lastSeenVersion + 1, currentVersion);

        // If there are no specific changelogs, do not show anything
        if (changelogs.isEmpty()) {
            saveLastSeenVersion(currentVersion);
            return;
        }

        // Build the dialog message.
        StringBuilder message = new StringBuilder();
        for (String log : changelogs) {
            message.append(log).append("\n-------\n"); // Add a bullet point for each entry
        }

        // Create and show the AlertDialog dialog.
        new AlertDialog.Builder(activity)
                .setTitle(context.getString(R.string.changelog_title)) // Dialog title
                .setMessage(message.toString()) // Message with changelogs
                .setPositiveButton(context.getString(R.string.dismiss), (dialog, which) -> {
                    // When the user presses "Dismiss", save the current version as the last seen.
                    saveLastSeenVersion(currentVersion);
                    Timber.d(TAG+ ": Version " + currentVersion + " saved as last seen.");
                    dialog.dismiss(); // Close the dialog
                })
                .setNegativeButton(context.getString(R.string.remember_later), (dialog, which) -> {
                    // If the user presses "Remind me later", simply close the dialog.
                    // The version is not saved, so the dialog will reappear on the next launch.
                    dialog.dismiss(); // Close the dialog
                })
                .setCancelable(false) // Makes the dialog non-cancelable by outside touch or back button
                .show(); // Show the dialog
    }

    /**
     * Retrieves the last seen app version by the user
     * from SharedPreferences.
     *
     * @return The last seen version, or 0 if it has never been saved.
     */
    private int getLastSeenVersion() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Returns the saved value, or 0 if it doesn't exist (first run).
        return prefs.getInt(KEY_LAST_SEEN_VERSION, 0);
    }

    /**
     * Saves the current app version to SharedPreferences
     * as the last version seen by the user.
     *
     * @param version The version to save.
     */
    private void saveLastSeenVersion(int version) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_LAST_SEEN_VERSION, version);
        editor.apply(); // Apply changes in the background
    }

    /**
     * Retrieves the current app version code from PackageManager.
     *
     * @return The current app version code, or -1 in case of error.
     */
    private int getCurrentAppVersion() {
        Core core = new Core(context);
        return core.getAppVersionCode();
    }

    /**
     * Retrieves changelog strings from R.string.changelog_XXXX
     * for a range of versions.
     *
     * @param startVersion The starting version (inclusive) from which to search for changelogs.
     * @param endVersion   The ending version (inclusive) up to which to search for changelogs.
     * @return A list of found changelog strings.
     */
    private List<String> getChangelogStrings(int startVersion, int endVersion) {
        List<String> changelogs = new ArrayList<>();
        // Iterate from startVersion to endVersion to retrieve the strings.
        for (int i = startVersion; i <= endVersion; i++) {
            // Build the string resource name, e.g., "changelog_1081".
            String resourceName = "changelog_" + i;
            // Get the ID of the string resource.
            @SuppressLint("DiscouragedApi") int resourceId = context.getResources().getIdentifier(resourceName, "string", context.getPackageName());

            // If the resource exists (ID is not 0), retrieve the string and add it to the list.
            if (resourceId != 0) {
                changelogs.add(context.getString(resourceId));
                Timber.d(TAG+ ": Changelog found for version " + i);
            } else {
                Timber.d(TAG+ ": No changelog found for version " + i);
            }
        }
        return changelogs;
    }

}