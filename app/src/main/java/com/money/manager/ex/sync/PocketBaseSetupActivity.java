package com.money.manager.ex.sync;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.settings.SyncPreferences;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Activity for setting up PocketBase synchronization.
 */
public class PocketBaseSetupActivity extends AppCompatActivity {

    private TextInputEditText mEditTextUrl, mEditTextEmail, mEditTextPassword;
    private Button mButtonConnect;
    private ProgressBar mProgressBar;
    private TextView mTextViewStatus;
    private CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pocketbase_setup);

        mEditTextUrl = findViewById(R.id.editTextUrl);
        mEditTextEmail = findViewById(R.id.editTextEmail);
        mEditTextPassword = findViewById(R.id.editTextPassword);
        mButtonConnect = findViewById(R.id.buttonConnect);
        mProgressBar = findViewById(R.id.progressBar);
        mTextViewStatus = findViewById(R.id.textViewStatus);

        // TODO: remove this in production
        // Pre-fill with temporary debug values
        mEditTextUrl.setText("http://192.168.1.20:8090");
        mEditTextEmail.setText("test.user@yourdomain.com");
        mEditTextPassword.setText("12345678");

        mButtonConnect.setOnClickListener(v -> startSetup());
    }

    @Override
    protected void onDestroy() {
        mDisposables.clear();
        super.onDestroy();
    }

    private void startSetup() {
        String url = mEditTextUrl.getText().toString().trim();
        String email = mEditTextEmail.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save URL temporarily to initialize client
        SyncPreferences prefs = new SyncPreferences(this);
        prefs.set(getString(R.string.pref_sync_url), url);

        setLoading(true);
        mTextViewStatus.setText("Authenticating...");

        mDisposables.add(Observable.fromCallable(() -> {
            PocketBaseClient client = PocketBaseClient.getInstance(this);
            return client.authenticate(email, password);
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
            if (success) {
                mTextViewStatus.setText("Authentication successful. Initializing database...");
                performInitialPull();
            } else {
                setLoading(false);
                mTextViewStatus.setText("Authentication failed. Check credentials and URL.");
            }
        }, throwable -> {
            setLoading(false);
            mTextViewStatus.setText("Error: " + throwable.getMessage());
            Timber.e(throwable);
        }));
    }

    private void performInitialPull() {
        mTextViewStatus.setText("Performing initial pull (this may take a while)...");

        mDisposables.add(Observable.fromCallable(() -> {
            SyncPreferences prefs = new SyncPreferences(this);
            prefs.setPocketBaseSyncEnabled(true); // Enable cloud mode to use the correct schema/engine
            
            PocketBaseSyncEngine engine = new PocketBaseSyncEngine(this);
            engine.synchronize(); // This will perform full pull if pb_last_sync_time is empty
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
            setLoading(false);
            mTextViewStatus.setText("Sync Setup Complete!");
            Toast.makeText(this, "Setup Successful", Toast.LENGTH_LONG).show();
            
            // Restart MainActivity to refresh database connection and UI
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish();
        }, throwable -> {
            setLoading(false);
            mTextViewStatus.setText("Sync failed: " + throwable.getMessage());
            Timber.e(throwable);
        }));
    }

    private void setLoading(boolean loading) {
        mProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        mButtonConnect.setEnabled(!loading);
        mEditTextUrl.setEnabled(!loading);
        mEditTextEmail.setEnabled(!loading);
        mEditTextPassword.setEnabled(!loading);
    }
}
