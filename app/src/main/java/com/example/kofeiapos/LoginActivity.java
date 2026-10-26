package com.example.kofeiapos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    DBHelper db;
    EditText etUser, etPass;
    TextView btnLogin, tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Ui.setup(this, findViewById(R.id.root));

        db = new DBHelper(this);
        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);

        btnLogin.setOnClickListener(v -> login());
        etPass.setOnEditorActionListener((v, actionId, event) -> {   // Done key on keyboard
            login();
            return true;
        });
    }

    void login() {
        String user = etUser.getText().toString().trim();
        String pass = etPass.getText().toString();

        if (db.checkAdmin(user, pass)) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();   // so Back does not return to the login
        } else {
            tvError.setText("Wrong username or password");
        }
    }
}
