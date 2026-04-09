package com.example.smartlocationreminder;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    EditText etUser, etPass, etConfirmPass;
    Button btnSignup;
    TextView tvGoToLogin;
    DatabaseHelper myDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUser = findViewById(R.id.etSignupUsername);
        etPass = findViewById(R.id.etSignupPassword);
        etConfirmPass = findViewById(R.id.etSignupConfirmPassword);
        btnSignup = findViewById(R.id.btnSignup);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
        myDb = new DatabaseHelper(this);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = etUser.getText().toString();
                String pass = etPass.getText().toString();
                String repass = etConfirmPass.getText().toString();

                if (user.isEmpty() || pass.isEmpty() || repass.isEmpty()) {
                    Toast.makeText(SignUpActivity.this, "الرجاء ملء جميع الحقول", Toast.LENGTH_SHORT).show();
                } else {
                    if (pass.equals(repass)) {
                        // هنا نستخدم الدالة الموجودة في DatabaseHelper
                        // إذا كانت الدالة غير موجودة، سنقوم بإضافة المستخدم مباشرة
                        // وقاعدة البيانات ستعيد false إذا فشل الإدخال
                        boolean insert = myDb.insertUser(user, pass);
                        if (insert) {
                            Toast.makeText(SignUpActivity.this, "تم إنشاء الحساب بنجاح!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "فشل التسجيل (قد يكون الاسم مستخدماً)", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SignUpActivity.this, "كلمتا المرور غير متطابقتين!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}