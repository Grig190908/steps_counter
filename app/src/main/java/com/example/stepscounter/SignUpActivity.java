package com.example.stepscounter;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private EditText emailSignUp, passwordSignUp, confirmPasswordSignUp;
    private Button btnSignUp;
    private TextView textSignIn;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailSignUp = findViewById(R.id.emailSignUp);
        passwordSignUp = findViewById(R.id.passwordSignUp);
        confirmPasswordSignUp = findViewById(R.id.confirmPasswordSignUp);
        btnSignUp = findViewById(R.id.btnSignUp);
        textSignIn = findViewById(R.id.textSignIn);
        progressBar = findViewById(R.id.progressBar);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailSignUp.getText().toString().trim();
                String password = passwordSignUp.getText().toString().trim();
                String confirmPassword = confirmPasswordSignUp.getText().toString().trim();

                if (!validateInputs(email, password, confirmPassword)) return;
                createUser(email, password);
            }
        });

        textSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignUpActivity.this, SignInActivity.class));
            }
        });
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SignUpActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.length() < 6) {
            Toast.makeText(SignUpActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnSignUp.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        btnSignUp.setEnabled(true);

                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SignUpActivity.this,
                                                            "Registration successful! Please verify your email.",
                                                            Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(SignUpActivity.this,
                                                            "Failed to send verification email: " + task.getException().getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignUpActivity.this, "Sign Up Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}