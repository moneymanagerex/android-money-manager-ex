package com.money.manager.ex.sync;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.google.android.material.textfield.TextInputEditText;
import com.money.manager.ex.R;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.scheduled.ScheduledTransactionForecastListServices;
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
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pocketbase_setup);

        View mainView = findViewById(R.id.main_layout);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        mEditTextUrl = findViewById(R.id.editTextUrl);
        mEditTextEmail = findViewById(R.id.editTextEmail);
        mEditTextPassword = findViewById(R.id.editTextPassword);
        mButtonConnect = findViewById(R.id.buttonConnect);
        mProgressBar = findViewById(R.id.progressBar);
        mTextViewStatus = findViewById(R.id.textViewStatus);

        loadSavedCredentials();

        mButtonConnect.setOnClickListener(v -> startSetup());

        // Se abbiamo già URL ed Email, proviamo un refresh silenzioso del token
        attemptSilentLogin();

        // Handle back button to reset cloud sync flag if setup is not finished
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancelSetupAndExit();
            }
        });
    }

    @Override
    protected void onDestroy() {
        mDisposables.clear();
        super.onDestroy();
    }

    private void loadSavedCredentials() {
        SyncPreferences prefs = new SyncPreferences(this);
        String savedUrl = prefs.loadPreference(R.string.pref_sync_url, "");
        String savedEmail = prefs.get(R.string.pref_pocketbase_email, "");

        if (!TextUtils.isEmpty(savedUrl)) {
            mEditTextUrl.setText(savedUrl);
        }
        if (!TextUtils.isEmpty(savedEmail)) {
            mEditTextEmail.setText(savedEmail);
        }
    }

    private void attemptSilentLogin() {
        SyncPreferences prefs = new SyncPreferences(this);
        String url = prefs.loadPreference(R.string.pref_sync_url, "");
        String email = prefs.get(R.string.pref_pocketbase_email, "");

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(email)) return;

        setLoading(true);
        mTextViewStatus.setText(R.string.checking_session);

        mDisposables.add(Observable.fromCallable(() -> {
            PocketBaseClient client = PocketBaseClient.getInstance(this);
            // Se siamo già autenticati (token presente), proviamo il refresh
            if (client.isAuthenticated()) {
                return client.refreshToken();
            }
            return false;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
            if (success) {
                mTextViewStatus.setText(R.string.session_restored_initializing_database);
                performInitialPull();
            } else {
                setLoading(false);
                mTextViewStatus.setText(R.string.please_enter_your_password_to_continue);
                mEditTextPassword.requestFocus();
            }
        }, throwable -> {
            setLoading(false);
            mTextViewStatus.setText(R.string.session_expired_please_login_again);
            Timber.e(throwable);
        }));
    }

    private void startSetup() {
        String url = mEditTextUrl.getText().toString().trim();
        String email = mEditTextEmail.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save URL and Email in preferences
        SyncPreferences prefs = new SyncPreferences(this);
        prefs.set(getString(R.string.pref_sync_url), url);
        prefs.set(getString(R.string.pref_pocketbase_email), email);

        setLoading(true);
        mTextViewStatus.setText(R.string.authenticating);

        mDisposables.add(Observable.fromCallable(() -> {
            PocketBaseClient client = PocketBaseClient.getInstance(this);
            return client.authenticate(email, password);
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
            if (success) {
                mTextViewStatus.setText(R.string.authentication_successful_initializing_database);
                performInitialPull();
            } else {
                setLoading(false);
                mTextViewStatus.setText(R.string.authentication_failed_check_credentials_and_url);
            }
        }, throwable -> {
            setLoading(false);
            mTextViewStatus.setText(getString(R.string.error_two_dots) + throwable.getMessage());
            Timber.e(throwable);
        }));
    }

    private void performInitialPull() {
        mTextViewStatus.setText(R.string.performing_initial_pull_this_may_take_a_while);

        mDisposables.add(Observable.fromCallable(() -> {
            SyncPreferences prefs = new SyncPreferences(this);
            prefs.setPocketBaseSyncEnabled(true); // Enable cloud mode to use the correct schema/engine
            // create db
                    MmxOpenHelper openHelper = new MmxOpenHelper(this, new DatabaseManager(this).getDatabasePath() );
                    SupportSQLiteDatabase db = openHelper.getWritableDatabase();
                    db.close();

                    PocketBaseSyncEngine engine = new PocketBaseSyncEngine(this);
            ScheduledTransactionForecastListServices.destroyInstance();
            engine.synchronize(); // This will perform full pull if pb_last_sync_time is empty
            return true;
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
            setLoading(false);
            mTextViewStatus.setText(R.string.sync_setup_complete);
            Toast.makeText(this, "Setup Successful", Toast.LENGTH_LONG).show();
            
            // Restart MainActivity to refresh database connection and UI
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            finish();
        }, throwable -> {
            setLoading(false);
            // In case of error, ensure the cloud mode is disabled so it can be retried or fallback to local
            new SyncPreferences(this).setPocketBaseSyncEnabled(false);
            mTextViewStatus.setText(getString(R.string.sync_failed) + throwable.getMessage());
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

    private void cancelSetupAndExit() {
        // Important: Reset the cloud sync flag if we are exiting without finishing the setup.
        // This prevents MainActivity from trying to open a half-configured cloud database.
        new SyncPreferences(this).setPocketBaseSyncEnabled(false);
        finish();
    }
}
