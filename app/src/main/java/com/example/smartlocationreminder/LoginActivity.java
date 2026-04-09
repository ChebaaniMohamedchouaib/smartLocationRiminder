package com.example.smartlocationreminder;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText etUsername, etPassword;
    Button btnLogin;
    TextView tvGoToSignup;
    DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvGoToSignup = findViewById(R.id.tvGoToSignup);
        myDb = new DatabaseHelper(this);

        btnLogin.setOnClickListener(v -> {
            String user = etUsername.getText().toString();
            String pass = etPassword.getText().toString();

            if (myDb.checkUser(user, pass)) {
                // حفظ اسم المستخدم للجلسة الحالية
                getSharedPreferences("UserSession", MODE_PRIVATE).edit().putString("username", user).apply();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "بيانات خاطئة", Toast.LENGTH_SHORT).show();
            }
        });

        tvGoToSignup.setOnClickListener(v -> startActivity(new Intent(this, SignUpActivity.class)));
    }
}