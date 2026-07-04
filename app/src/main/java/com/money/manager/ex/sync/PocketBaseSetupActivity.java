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
import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.R;
import com.money.manager.ex.core.database.DatabaseManager;
import com.money.manager.ex.database.MmxOpenHelper;
import com.money.manager.ex.home.DatabaseMetadata;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.home.RecentDatabasesProvider;
import com.money.manager.ex.scheduled.ScheduledTransactionForecastListServices;
import com.money.manager.ex.settings.AppSettings;
import com.money.manager.ex.settings.DatabaseSettings;
import com.money.manager.ex.settings.SyncPreferences;

import java.util.Objects;

import javax.inject.Inject;

import dagger.Lazy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Activity for setting up PocketBase synchronization.
 */
public class PocketBaseSetupActivity extends AppCompatActivity {

    public static final String EXTRA_RE_LOGIN = "PocketBaseSetupActivity:ReLogin";

    private TextInputEditText mEditTextUrl, mEditTextEmail, mEditTextPassword;
    private Button mButtonConnect;
    private ProgressBar mProgressBar;
    private TextView mTextViewStatus;
    private final CompositeDisposable mDisposables = new CompositeDisposable();

    @Inject
    Lazy<RecentDatabasesProvider> mDatabasesLazy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pocketbase_setup);

        MmexApplication.getApp().iocComponent.inject(this);

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
        boolean isReLogin = getIntent().getBooleanExtra(EXTRA_RE_LOGIN, false);
        DatabaseMetadata current = mDatabasesLazy.get().getCurrent();
        if (current == null) return;

        mEditTextEmail.setText(current.getRemoteUser());
        mEditTextUrl.setText(current.getRemoteURL());

        if (isReLogin) {
                // Disable fields in re-login mode
                mEditTextUrl.setEnabled(false);
                mEditTextEmail.setEnabled(false);
                mEditTextPassword.requestFocus();
                return;
        }

    }

    private void attemptSilentLogin() {
        SyncPreferences prefs = new SyncPreferences(this);
        DatabaseMetadata current = mDatabasesLazy.get().getCurrent();
        if (current == null) return;

        String url = current.getRemoteURL();
        String email = current.getRemoteUser();

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
        String url = Objects.requireNonNull(mEditTextUrl.getText()).toString().trim();
        String email = Objects.requireNonNull(mEditTextEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(mEditTextPassword.getText()).toString().trim();

        if (TextUtils.isEmpty(url) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        mTextViewStatus.setText(R.string.authenticating);

        mDisposables.add(Observable.fromCallable(() -> {
            PocketBaseClient client = PocketBaseClient.getInstance(this);
            return client.authenticate(url, email, password);
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(success -> {
            if (success) {
                boolean isReLogin = getIntent().getBooleanExtra(EXTRA_RE_LOGIN, false);
                // update medatada in a safe way
                DatabaseMetadata metadata = mDatabasesLazy.get().getCurrent();
                metadata.setRemoteServer(DatabaseMetadata.POCKETBASE,email,url);
                mDatabasesLazy.get().remove(metadata.localPath);
                mDatabasesLazy.get().add(metadata);

                if (isReLogin) {
                    setLoading(false);
                    mTextViewStatus.setText(R.string.sync_setup_complete);
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    mTextViewStatus.setText(R.string.authentication_successful_initializing_database);
                    performInitialPull();
                }
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

        String url = Objects.requireNonNull(mEditTextUrl.getText()).toString().trim();
        String email = Objects.requireNonNull(mEditTextEmail.getText()).toString().trim();

        mDisposables.add(Observable.fromCallable(() -> {
            // create db
            String dbPath = new DatabaseManager(this).getDatabasePath();
            MmxOpenHelper openHelper = new MmxOpenHelper(this, dbPath);
            SupportSQLiteDatabase db = openHelper.getWritableDatabase();
            db.close();

            // Setup metadata for the new cloud database
            android.net.Uri inputUri = android.net.Uri.parse(url);

            DatabaseMetadata metadata = new DatabaseMetadata();
            metadata.localPath = dbPath;
            metadata.setRemoteServer(DatabaseMetadata.POCKETBASE, email, url);
            metadata.localSnapshotTimestamp = java.time.Instant.now().toString();

            mDatabasesLazy.get().add(metadata);
            DatabaseSettings dbSetting = (new AppSettings(this).getDatabaseSettings());
            dbSetting.setDatabasePath(dbPath);

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
        finish();
    }
}
