package com.money.manager.ex.sync;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.money.manager.ex.R;
import com.money.manager.ex.core.docstorage.FileStorageHelper;
import com.money.manager.ex.database.PasswordActivity;
import com.money.manager.ex.settings.DatabaseSettingsFragment;
import com.money.manager.ex.settings.SettingsActivity;

public class SyncDialogMessage extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int title =  R.string.remote_unavailable;
        int body = R.string.request_reopen;

        Intent intent = getIntent();
        if (intent != null) {
            title = intent.getIntExtra("TITLE", R.string.remote_unavailable);
            body = intent.getIntExtra("BODY", R.string.request_reopen);
        }

        notifyUserSyncFailed(getApplicationContext(), title, body );
    }


    public void notifyUserSyncFailed(Context context, int resTitle, int resBody) {
        // open setting for export
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(resTitle);
        builder.setMessage(resBody);
        builder.setPositiveButton(R.string.menu_move_database_to_external_storage, (dialog, which) -> {
            Intent intent = new Intent(context, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_FRAGMENT, DatabaseSettingsFragment.class.getSimpleName());
            context.startActivity(intent);
        });
        builder.setNegativeButton(R.string.open_database, (dialog, which) -> {
            context.startActivity(new Intent(context, PasswordActivity.class));
            FileStorageHelper helper = new FileStorageHelper(context);
            helper.showStorageFilePicker();
        });
        builder.show();
    }

}
