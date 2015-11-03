package com.money.manager.ex.database;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.money.manager.ex.R;
import com.money.manager.ex.home.MainActivity;

public class PasswordActivity
    extends Activity {

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

        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
