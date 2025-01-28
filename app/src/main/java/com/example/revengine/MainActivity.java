package com.example.revengine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private CheckBox checkboxShowPassword;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progress_bar);
        checkboxShowPassword = findViewById(R.id.checkbox_show_password);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Check if the user is already logged in
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            // Check if the user is verified
            if (currentUser.isEmailVerified()) {
                checkUserInFirestore(currentUser.getUid());
            } else {
                Toast.makeText(MainActivity.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                firebaseAuth.signOut(); // Sign out the user if email is not verified
            }
        }

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter all the fields", Toast.LENGTH_SHORT).show();
            } else {
                progressBar.setVisibility(View.VISIBLE);
                simulateLogin(username, password);
            }
        });

        checkboxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
            } else {
                etPassword.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
    }

    private void simulateLogin(String username, String password) {
        new Handler().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);

            if (username.equals("admin") && password.equals("admin")) {
                Intent intent = new Intent(MainActivity.this, AdminHomeActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(MainActivity.this, "Welcome admin!", Toast.LENGTH_SHORT).show();
            } else {
                firebaseAuth.signInWithEmailAndPassword(username, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                if (user != null) {
                                    // Check if the user is verified
                                    if (user.isEmailVerified()) {
                                        checkUserInFirestore(user.getUid());
                                    } else {
                                        Toast.makeText(MainActivity.this, "Please verify your email first.", Toast.LENGTH_SHORT).show();
                                        firebaseAuth.signOut(); // Sign out the user if email is not verified
                                    }
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "Invalid email and password!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }, 2000);
    }

    private void checkUserInFirestore(String userId) {
        firestore.collection("Users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            Intent intent = new Intent(MainActivity.this, ProductsDisplay.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(MainActivity.this, "Welcome valid user!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "User not found in database!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Database error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
