package com.example.stepscounter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailSignUp, passwordSignUp, confirmPasswordSignUp; // Fixed issue
    private Button btnSignUp;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components (Fixed issue: confirmPasswordSignUp now exists)
        emailSignUp = findViewById(R.id.emailSignUp);
        passwordSignUp = findViewById(R.id.passwordSignUp);
        confirmPasswordSignUp = findViewById(R.id.confirmPasswordSignUp); // Fixed error
        btnSignUp = findViewById(R.id.btnSignUp);
        progressBar = findViewById(R.id.progressBar);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUpUser();
            }
        });
    }

    private void signUpUser() {
        String email = emailSignUp.getText().toString().trim();
        String password = passwordSignUp.getText().toString().trim();
        String confirmPassword = confirmPasswordSignUp.getText().toString().trim();

        // Input validation
        if (email.isEmpty()) {
            emailSignUp.setError("Email is required!");
            emailSignUp.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailSignUp.setError("Please enter a valid email!");
            emailSignUp.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordSignUp.setError("Password is required!");
            passwordSignUp.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordSignUp.setError("Password must be at least 6 characters!");
            passwordSignUp.requestFocus();
            return;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordSignUp.setError("Passwords do not match!");
            confirmPasswordSignUp.requestFocus();
            return;
        }

        // Show progress bar and create user
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SignUpActivity.this, "Verification email sent to " + user.getEmail(), Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(SignUpActivity.this, "Failed to send verification email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}