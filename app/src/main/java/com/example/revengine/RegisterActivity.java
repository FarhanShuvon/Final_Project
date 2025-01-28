package com.example.revengine;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword, etMobile;
    private Button btnRegister, btnLogin;
    private CheckBox checkboxShowPassword;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.et_register_username);
        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        etConfirmPassword = findViewById(R.id.et_register_confirm_password);
        etMobile = findViewById(R.id.et_register_mobile);
        btnRegister = findViewById(R.id.btn_register);
        btnLogin = findViewById(R.id.btn_login);
        checkboxShowPassword = findViewById(R.id.checkbox_show_password);
        progressBar = findViewById(R.id.progress_bar);

        checkboxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });

        btnRegister.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();

            if (validateInputs(username, email, password, confirmPassword, mobile)) {
                progressBar.setVisibility(View.VISIBLE);
                registerUser(username, email, password, mobile);
            }
        });

        btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private boolean validateInputs(String username, String email, String password, String confirmPassword, String mobile) {
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty() || username.isEmpty() || email.isEmpty() || mobile.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.equals(password)) {
            Toast.makeText(this, "Username and password cannot be the same", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (Character.isDigit(username.charAt(0))) {
            Toast.makeText(this, "Username cannot start with a number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!containsUpperCase(password)) {
            Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!containsLowerCase(password)) {
            Toast.makeText(this, "Password must contain at least one lowercase letter", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!containsDigit(password)) {
            Toast.makeText(this, "Password must contain at least one digit", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!containsSpecialChar(password)) {
            Toast.makeText(this, "Password must contain at least one special character", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser(String username, String email, String password, String mobile) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseAuth.getInstance().getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        String userId = firebaseAuth.getCurrentUser().getUid();
                                        User user = new User(username, email, password, mobile);

                                        firestore.collection("Users").document(userId).set(user)
                                                .addOnCompleteListener(task1 -> {
                                                    if (task1.isSuccessful()) {
                                                        Toast.makeText(RegisterActivity.this, "Registration successful. Please verify your email.", Toast.LENGTH_SHORT).show();
                                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Toast.makeText(RegisterActivity.this, "Failed to save user in Firestore", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Email verification failed: " + verificationTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean containsUpperCase(String password) {
        return !password.equals(password.toLowerCase());
    }

    private boolean containsLowerCase(String password) {
        return !password.equals(password.toUpperCase());
    }

    private boolean containsDigit(String password) {
        Pattern digitPattern = Pattern.compile("[0-9]");
        Matcher matcher = digitPattern.matcher(password);
        return matcher.find();
    }

    private boolean containsSpecialChar(String password) {
        Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9]");
        Matcher matcher = specialCharPattern.matcher(password);
        return matcher.find();
    }

    static class User {
        private String username;
        private String email;
        private String password;
        private String mobile;

        public User(String username, String email, String password, String mobile) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.mobile = mobile;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }
    }
}
