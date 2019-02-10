/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.database;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;

import androidx.appcompat.app.AppCompatActivity;

public class PasswordActivity
    extends AppCompatActivity {

    public static final String EXTRA_PASSWORD = "password";

    private String dbPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        // get dbPath
        this.dbPath = getIntent().getStringExtra(MainActivity.EXTRA_DATABASE_PATH);

        initializeOkButton();
    }

    private void initializeOkButton() {
        Button okButton = (Button) this.findViewById(R.id.btnSubmit);
        if (okButton != null) {
            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    returnPassword();
                }
            });
        }
    }

    private void returnPassword() {
        EditText txt = (EditText) this.findViewById(R.id.txtPassword);
        String password = txt.getText().toString();

        Intent result = new Intent();
        result.putExtra(EXTRA_PASSWORD, password);
        result.putExtra(MainActivity.EXTRA_DATABASE_PATH, this.dbPath);

        setResult(AppCompatActivity.RESULT_OK, result);
        finish();
    }
}
