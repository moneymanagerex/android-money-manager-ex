package com.money.manager.ex.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import com.google.gson.JsonObject;
import com.money.manager.ex.settings.SyncPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Client for PocketBase API.
 * Handles Retrofit initialization, dual-mode authentication (users/superusers), and token refresh.
 */
public class PocketBaseClient {

    private static PocketBaseClient mInstance;
    private final Context mContext;
    private PocketBaseApiService mService;
    private String mAuthToken;
    private String mCurrentBaseUrl;
    private String mAuthCollection; // "users" or "_superusers"
    private SharedPreferences mEncryptedPrefs;

    private static final String PREF_AUTH_TOKEN = "pb_auth_token";
    private static final String PREF_AUTH_COLLECTION = "pb_auth_collection";

    private PocketBaseClient(Context context) {
        mContext = context.getApplicationContext();
        initEncryptedPrefs();
        initializeService();
        
        // Load session from secure storage
        mAuthToken = mEncryptedPrefs.getString(PREF_AUTH_TOKEN, null);
        mAuthCollection = mEncryptedPrefs.getString(PREF_AUTH_COLLECTION, "users");
    }

    private void initEncryptedPrefs() {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            mEncryptedPrefs = EncryptedSharedPreferences.create(
                    "secure_sync_prefs",
                    masterKeyAlias,
                    mContext,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            Timber.e(e, "Error initializing EncryptedSharedPreferences, falling back to standard");
            mEncryptedPrefs = mContext.getSharedPreferences("secure_sync_prefs_fallback", Context.MODE_PRIVATE);
        }
    }

    private void initializeService() {
        SyncPreferences preferences = new SyncPreferences(mContext);
        String baseUrl = preferences.loadPreference(com.money.manager.ex.R.string.pref_sync_url, "");
        
        if (TextUtils.isEmpty(baseUrl)) {
            Timber.w("PocketBase base URL is empty");
            return;
        }

        // Add protocol if missing
        if (!baseUrl.startsWith("http://") && !baseUrl.startsWith("https://")) {
            if (baseUrl.startsWith("192.168.") || baseUrl.startsWith("10.") || baseUrl.startsWith("localhost") || baseUrl.startsWith("127.0.0.1")) {
                baseUrl = "http://" + baseUrl;
            } else {
                baseUrl = "https://" + baseUrl;
            }
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        mCurrentBaseUrl = baseUrl;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();

        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            mService = retrofit.create(PocketBaseApiService.class);
        } catch (IllegalArgumentException e) {
            Timber.e(e, "Invalid base URL: %s", baseUrl);
        }
    }

    public static synchronized PocketBaseClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PocketBaseClient(context);
        } else {
            // Check if URL has changed in preferences
            SyncPreferences preferences = new SyncPreferences(context);
            String savedUrl = preferences.loadPreference(com.money.manager.ex.R.string.pref_sync_url, "");
            if (!TextUtils.isEmpty(savedUrl) && mInstance.mCurrentBaseUrl != null && !mInstance.mCurrentBaseUrl.contains(savedUrl)) {
                mInstance.initializeService();
            }
        }
        return mInstance;
    }

    public PocketBaseApiService getService() {
        return mService;
    }

    public boolean isAuthenticated() {
        return mAuthToken != null;
    }

    private void saveSession(String token, String collection) {
        mAuthToken = token;
        mAuthCollection = collection;
        mEncryptedPrefs.edit()
                .putString(PREF_AUTH_TOKEN, token)
                .putString(PREF_AUTH_COLLECTION, collection)
                .apply();
    }

    public void clearSession() {
        mAuthToken = null;
        mAuthCollection = "users";
        mEncryptedPrefs.edit()
                .remove(PREF_AUTH_TOKEN)
                .remove(PREF_AUTH_COLLECTION)
                .apply();
    }

    /**
     * Attempts authentication. First tries as standard user, then as superuser.
     */
    public boolean authenticate(String email, String password) {
        if (mService == null) {
            initializeService();
            if (mService == null) return false;
        }

        JsonObject body = new JsonObject();
        body.addProperty("identity", email);
        body.addProperty("password", password);

        // 1. Try as standard user
        if (attemptAuth(body, "users")) {
            return true;
        }

        // 2. Try as superuser
        return attemptAuth(body, "_superusers");
    }

    private boolean attemptAuth(JsonObject body, String collection) {
        try {
            retrofit2.Response<JsonObject> response = mService.authWithPassword(collection, body).execute();
            if (response.isSuccessful() && response.body() != null) {
                saveSession(response.body().get("token").getAsString(), collection);
                Timber.d("[SYNC_CLOUD] Authenticated successfully as %s", collection);
                return true;
            }
        } catch (IOException e) {
            Timber.e(e, "[SYNC_CLOUD] Error during auth attempt for %s", collection);
        }
        return false;
    }

    public boolean refreshToken() {
        if (mService == null || mAuthToken == null || mAuthCollection == null) return false;

        try {
            retrofit2.Response<JsonObject> response = mService.authRefresh(mAuthCollection).execute();
            if (response.isSuccessful() && response.body() != null) {
                saveSession(response.body().get("token").getAsString(), mAuthCollection);
                Timber.d("[SYNC_CLOUD] Token refreshed successfully for %s", mAuthCollection);
                return true;
            } else {
                Timber.w("[SYNC_CLOUD] Token refresh failed for %s: %d", mAuthCollection, response.code());
                clearSession();
            }
        } catch (IOException e) {
            Timber.e(e, "[SYNC_CLOUD] Error refreshing token");
        }
        return false;
    }

    private class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            // Do not add auth header for auth requests
            String path = originalRequest.url().encodedPath();
            if (mAuthToken == null || path.contains("/auth-with-password") || path.contains("/auth-refresh")) {
                return chain.proceed(originalRequest);
            }

            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", mAuthToken)
                    .build();
            return chain.proceed(newRequest);
        }
    }
}
