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

package com.money.manager.ex.passcode;

import android.os.Build;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

/**
 * Wrapper for the encryption class/library.
 * Ref: https://android.googlesource.com/platform/development/+/master/samples/BrokenKeyDerivation/src/com/example/android/brokenkeyderivation/BrokenKeyDerivationActivity.java
 */

public class Encryptor {

    private static final int KEY_SIZE = 32;

    public String encrypt(String text) {
        return "not implemented";
    }

    /**
     * Method used to derive an <b>insecure</b> key by emulating the SHA1PRNG algorithm from the
     * deprecated Crypto provider.
     *
     * Do not use it to encrypt new data, just to decrypt encrypted data that would be unrecoverable
     * otherwise.
     */
    public static SecretKey deriveKeyInsecurely(String password, int keySizeInBytes) {
        byte[] passwordBytes;
        if (Build.VERSION.SDK_INT >= 19) {
            passwordBytes = password.getBytes(StandardCharsets.US_ASCII);
        } else {
            passwordBytes = password.getBytes(Charset.forName("UTF-8"));
        }

        return new SecretKeySpec(
                InsecureSHA1PRNGKeyDerivator.deriveInsecureKey(passwordBytes, keySizeInBytes), "AES");
    }

    /**
     * Example use of a key derivation function, derivating a key securely from a password.
     */
    private SecretKey deriveKeySecurely(String password, int keySizeInBytes) {
        // Use this to derive the key from the password:
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), retrieveSalt(),
                100 /* iterationCount */,
                keySizeInBytes * 8 /* key size in bits */);
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            throw new RuntimeException("Deal with exceptions properly!", e);
        }
    }

    /**
     * This is from the Android blog post.
     * @param password
     * @return
     */
    private SecretKey getKeyFor(String password) {
        /* User types in their password: */
//        String password = "password";

   /* Store these things on disk used to derive key later: */
        int iterationCount = 1000;
        int saltLength = 32; // bytes; should be the same size as the output (256 / 8 = 32)
        int keyLength = 256; // 256-bits for AES-256, 128-bits for AES-128, etc
        byte[] salt; // Should be of saltLength

   /* When first creating the key, obtain a salt with this: */
        SecureRandom random = new SecureRandom();
//        byte[] salt = new byte[saltLength];
        salt = new byte[saltLength];
        random.nextBytes(salt);

   /* Use this to derive the key from the password: */
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt,
                iterationCount, keyLength);

        byte[] keyBytes;
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        } catch (Exception e) {
            Timber.e(e, "generating key");
            return null;
        }
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        return key;
    }

//    /**
//     * Retrieve encrypted data using a password. If data is stored with an insecure key, re-encrypt
//     * with a secure key.
//     */
//    private String retrieveData(String password) {
//        String decryptedString;
//        if (isDataStoredWithInsecureKey()) {
//            SecretKey insecureKey = deriveKeyInsecurely(password, KEY_SIZE);
//            byte[] decryptedData = decryptData(retrieveEncryptedData(), retrieveIv(), insecureKey);
//            SecretKey secureKey = deriveKeySecurely(password, KEY_SIZE);
//            storeDataEncryptedWithSecureKey(encryptData(decryptedData, retrieveIv(), secureKey));
//            decryptedString = "Warning: data was encrypted with insecure key\n"
//                    + new String(decryptedData, StandardCharsets.UTF_8);
//        } else {
//            SecretKey secureKey = deriveKeySecurely(password, KEY_SIZE);
//            byte[] decryptedData = decryptData(retrieveEncryptedData(), retrieveIv(), secureKey);
//            decryptedString = "Great!: data was encrypted with secure key\n"
//                    + new String(decryptedData, StandardCharsets.UTF_8);
//        }
//        return decryptedString;
//    }

    private static byte[] encryptOrDecrypt(
            byte[] data, SecretKey key, byte[] iv, boolean isEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7PADDING");
            cipher.init(isEncrypt ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, key,
                    new IvParameterSpec(iv));
            return cipher.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException("This is unconceivable!", e);
        }
    }
    private static byte[] encryptData(byte[] data, byte[] iv, SecretKey key) {
        return encryptOrDecrypt(data, key, iv, true);
    }
    private static byte[] decryptData(byte[] data, byte[] iv, SecretKey key) {
        return encryptOrDecrypt(data, key, iv, false);
    }

    private byte[] retrieveSalt() {
        // Salt must be at least the same size as the key.
        byte[] salt = new byte[KEY_SIZE];
        // Create a random salt if encrypting for the first time, and update it for future use.
        readFromFileOrCreateRandom("salt", salt);
        return salt;
    }

    /**
     * Read from file or return random bytes in the given array.
     *
     * <p>Save to file if file didn't exist.
     */
    private void readFromFileOrCreateRandom(String fileName, byte[] bytes) {
//        if (fileExists(fileName)) {
//            readBytesFromFile(fileName, bytes);
//            return;
//        }
        SecureRandom sr = new SecureRandom();
        sr.nextBytes(bytes);
        // todo store.
        // writeToFile(fileName, bytes);
    }
}
