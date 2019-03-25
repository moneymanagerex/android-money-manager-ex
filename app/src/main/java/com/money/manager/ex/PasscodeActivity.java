/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.money.manager.ex;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.widget.Toast;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.log.ErrorRaisedEvent;
import com.money.manager.ex.passcode.FingerprintHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class PasscodeActivity extends AppCompatActivity {

	public static final String INTENT_REQUEST_PASSWORD = "com.money.manager.ex.custom.intent.action.REQUEST_PASSWORD";
	public static final String INTENT_MESSAGE_TEXT = "INTENT_MESSAGE_TEXT";
	public static final String INTENT_RESULT_PASSCODE = "INTENT_RESULT_PASSCODE";

	private static final String KEY_NAME = "yourKey";
	private Cipher cipher;
	private KeyStore keyStore;
	private KeyGenerator keyGenerator;
	private FingerprintManagerCompat.CryptoObject cryptoObject;
	private FingerprintManagerCompat fingerprintManager;
	private KeyguardManager keyguardManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// set theme
		try {
			UIHelper uiHelper = new UIHelper(getApplicationContext());
			setTheme(uiHelper.getThemeId());
		} catch (Exception e) {
			//Log.e(BaseListFragment.class.getSimpleName(), e.getMessage());
            Timber.e(e, "setting theme in passcode activity");
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.passcode_activity);

        ButterKnife.bind(this);

        // create a listener for button
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Button click = (Button) v;
				if (getWindow().getCurrentFocus() != null && getWindow().getCurrentFocus() instanceof EditText) {
					EditText getFocus = (EditText) getWindow().getCurrentFocus();
					if (getFocus != null && click.getTag() != null) {
						getFocus.setText(click.getTag().toString());
						//quick-fix convert 'switch' to 'if-else'
						if (getFocus.getId() == R.id.editTextPasscode1) {
							((EditText) findViewById(R.id.editTextPasscode2)).requestFocus();
						} else if (getFocus.getId() == R.id.editTextPasscode2) {
							((EditText) findViewById(R.id.editTextPasscode3)).requestFocus();
						} else if (getFocus.getId() == R.id.editTextPasscode3) {
							((EditText) findViewById(R.id.editTextPasscode4)).requestFocus();
						} else if (getFocus.getId() == R.id.editTextPasscode4) {
							((EditText) findViewById(R.id.editTextPasscode5)).requestFocus();
						} else if (getFocus.getId() == R.id.editTextPasscode5) {
							Intent result = new Intent();
							// set result
							result.putExtra(INTENT_RESULT_PASSCODE, ((EditText) findViewById(R.id.editTextPasscode1)).getText().toString()
									+ ((EditText) findViewById(R.id.editTextPasscode2)).getText().toString()
									+ ((EditText) findViewById(R.id.editTextPasscode3)).getText().toString()
									+ ((EditText) findViewById(R.id.editTextPasscode4)).getText().toString()
									+ ((EditText) findViewById(R.id.editTextPasscode5)).getText().toString());
							// return result
							setResult(RESULT_OK, result);
							finish();
						}
					}
				}
			}
		};

		// arrays of button id
		int ids[] = { R.id.buttonPasscode0, R.id.buttonPasscode1, R.id.buttonPasscode2,
			R.id.buttonPasscode3,
			R.id.buttonPasscode4, R.id.buttonPasscode5,
			R.id.buttonPasscode6, R.id.buttonPasscode7, R.id.buttonPasscode8, R.id.buttonPasscode9 };
		for (int i : ids) {
			Button button = (Button) findViewById(i);
			button.setOnClickListener(clickListener);
		}

		// textview message
		TextView textView = (TextView) findViewById(R.id.textViewMessage);
		textView.setText(null);

		// intent and action
		if (getIntent() != null && getIntent().getAction() != null) {
			if (INTENT_REQUEST_PASSWORD.equals(getIntent().getAction())) {
				if (getIntent().getStringExtra(INTENT_MESSAGE_TEXT) != null) {
					textView.setText(getIntent().getStringExtra(INTENT_MESSAGE_TEXT));
				}
			}
		}

        UIHelper ui = new UIHelper(this);
		ImageButton buttonKeyBack = (ImageButton) findViewById(R.id.buttonPasscodeKeyBack);
		buttonKeyBack.setImageDrawable(ui.getIcon(GoogleMaterial.Icon.gmd_backspace)
            .color(ui.getPrimaryTextColor()));

		//Handle fingerprint
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {

			keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
			//fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
			fingerprintManager = FingerprintManagerCompat.from(getApplicationContext());

			if (!fingerprintManager.isHardwareDetected()) {
				((ImageView) findViewById(R.id.fpImageView))
						.setVisibility(View.GONE);
				((com.money.manager.ex.view.RobotoTextView) findViewById(R.id.fingerprintInfo))
						.setVisibility(View.GONE); //.setText(R.string.fingerprint_no_hardware);
		}
			else {

				if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
					Toast.makeText(this, R.string.fingerprint_check_permission, Toast.LENGTH_LONG).show();
				}

				if (!fingerprintManager.hasEnrolledFingerprints()) {
					Toast.makeText(this, R.string.fingerprint_has_enrolled, Toast.LENGTH_LONG).show();
				}

				if (!keyguardManager.isKeyguardSecure()) {
					Toast.makeText(this, R.string.fingerprint_is_keyguard_secure, Toast.LENGTH_LONG).show();
				}
				else {
					try {
						generateKey();
					} catch (FingerprintException e) {
						e.printStackTrace();
					}
					if (initCipher()) {
						cryptoObject = new FingerprintManagerCompat.CryptoObject(cipher);
						FingerprintHandler helper = new FingerprintHandler(this);
						helper.startAuth(fingerprintManager, cryptoObject);
					}
				}
			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Subscribe
	public void onEvent(ErrorRaisedEvent event) {
		// display the error to the user
		new UIHelper(this).showToast(event.message);
	}

	@OnClick(R.id.buttonPasscodeKeyBack)
	public void onBackspaceClick() {
		EditText getFocus = (EditText) getWindow().getCurrentFocus();
		if (getFocus != null) {
			boolean nullRequestFocus = false;
			if (!TextUtils.isEmpty(getFocus.getText())) {
				getFocus.setText(null);
			} else nullRequestFocus = true;
			//quick-fix convert 'switch' to 'if-else'
			if (getFocus.getId() == R.id.editTextPasscode1) {
			} else if (getFocus.getId() == R.id.editTextPasscode2) {
				((EditText) findViewById(R.id.editTextPasscode1)).requestFocus();
				if (nullRequestFocus) {
					((EditText) findViewById(R.id.editTextPasscode1)).setText(null);
				}
			} else if (getFocus.getId() == R.id.editTextPasscode3) {
				((EditText) findViewById(R.id.editTextPasscode2)).requestFocus();
				if (nullRequestFocus) {
					((EditText) findViewById(R.id.editTextPasscode2)).setText(null);
				}
			} else if (getFocus.getId() == R.id.editTextPasscode4) {
				((EditText) findViewById(R.id.editTextPasscode3)).requestFocus();
				if (nullRequestFocus) {
					((EditText) findViewById(R.id.editTextPasscode3)).setText(null);
				}
			} else if (getFocus.getId() == R.id.editTextPasscode5) {
				((EditText) findViewById(R.id.editTextPasscode4)).requestFocus();
				if (nullRequestFocus) {
					((EditText) findViewById(R.id.editTextPasscode4)).setText(null);
				}
			}
		}
	}

	// Fingerprint methods
	private void generateKey() throws FingerprintException {
		try {

			keyStore = KeyStore.getInstance("AndroidKeyStore");
			keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

			keyStore.load(null);
			keyGenerator.init(new
					KeyGenParameterSpec.Builder(KEY_NAME,
					KeyProperties.PURPOSE_ENCRYPT |
							KeyProperties.PURPOSE_DECRYPT)
					.setBlockModes(KeyProperties.BLOCK_MODE_CBC)
					.setUserAuthenticationRequired(true)
					.setEncryptionPaddings(
							KeyProperties.ENCRYPTION_PADDING_PKCS7)
					.build());

			keyGenerator.generateKey();

		} catch (KeyStoreException
				| NoSuchAlgorithmException
				| NoSuchProviderException
				| InvalidAlgorithmParameterException
				| CertificateException
				| IOException exc) {
			exc.printStackTrace();
			throw new FingerprintException(exc);
		}

	}

	public boolean initCipher() {
		try {
			cipher = Cipher.getInstance(
					KeyProperties.KEY_ALGORITHM_AES + "/"
							+ KeyProperties.BLOCK_MODE_CBC + "/"
							+ KeyProperties.ENCRYPTION_PADDING_PKCS7);
		} catch (NoSuchAlgorithmException |
				NoSuchPaddingException e) {
			throw new RuntimeException("Failed to get Cipher", e);
		}

		try {
			keyStore.load(null);
			SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
					null);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			return true;
		} catch (KeyPermanentlyInvalidatedException e) {
			return false;
		} catch (KeyStoreException | CertificateException
				| UnrecoverableKeyException | IOException
				| NoSuchAlgorithmException | InvalidKeyException e) {
			throw new RuntimeException("Failed to init Cipher", e);
		}
	}

	private class FingerprintException extends Exception {

		public FingerprintException(Exception e) {
			super(e);
		}
	}

}
