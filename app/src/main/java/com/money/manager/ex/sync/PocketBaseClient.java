package com.money.manager.ex.sync;

import android.content.Context;

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
 * Handles Retrofit initialization and authentication.
 */
public class PocketBaseClient {

    private static PocketBaseClient mInstance;
    private final Context mContext;
    private final PocketBaseApiService mService;
    private String mAuthToken;

    private PocketBaseClient(Context context) {
        mContext = context.getApplicationContext();
        
        SyncPreferences preferences = new SyncPreferences(mContext);
        String baseUrl = preferences.loadPreference(com.money.manager.ex.R.string.pref_sync_url, "");
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor())
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mService = retrofit.create(PocketBaseApiService.class);
    }

    public static synchronized PocketBaseClient getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PocketBaseClient(context);
        }
        return mInstance;
    }

    public PocketBaseApiService getService() {
        return mService;
    }

    public boolean isAuthenticated() {
        return mAuthToken != null;
    }

    public boolean authenticate(String email, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("identity", email);
        body.addProperty("password", password);

        try {
            retrofit2.Response<JsonObject> response = mService.authWithPassword(body).execute();
            if (response.isSuccessful() && response.body() != null) {
                mAuthToken = response.body().get("token").getAsString();
                return true;
            }
        } catch (IOException e) {
            Timber.e(e, "[SYNC_CLOUD] Error authenticating with PocketBase");
        }
        return false;
    }

    private class AuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();
            if (mAuthToken == null || originalRequest.url().encodedPath().contains("auth-with-password")) {
                return chain.proceed(originalRequest);
            }

            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", mAuthToken)
                    .build();
            return chain.proceed(newRequest);
        }
    }
}
