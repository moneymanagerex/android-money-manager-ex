package com.money.manager.ex.passcode;

/**
 * Created by velmurug on 2018-04-15.
 */

import android.content.Context;
import android.content.Intent;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import androidx.core.os.CancellationSignal;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;
import com.money.manager.ex.PasscodeActivity;

public class FingerprintHandler extends FingerprintManagerCompat.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context context;

    public FingerprintHandler(Context mContext) {
        context = mContext;
    }

    public void startAuth(FingerprintManagerCompat manager, FingerprintManagerCompat.CryptoObject cryptoObject) {
        cancellationSignal = new CancellationSignal();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, 0, cancellationSignal, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        Toast.makeText(context, "Authentication Error:\n" + errString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        Toast.makeText(context, "Authentication failed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        Toast.makeText(context, "Authentication Help:\n" + helpString, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
        //Toast.makeText(context, "Authentication Success!", Toast.LENGTH_LONG).show();

        Intent data = new Intent();
        data.putExtra(PasscodeActivity.INTENT_RESULT_PASSCODE, "FingerprintAuthenticationSuccess");

        // return result
        ((AppCompatActivity) context).setResult(PasscodeActivity.RESULT_OK, data);
        ((AppCompatActivity) context).finish();

    }

}
