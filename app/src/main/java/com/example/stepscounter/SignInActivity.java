package com.example.stepscounter;

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

public class SignInActivity extends AppCompatActivity {

    private EditText emailSignIn, passwordSignIn;
    private Button btnSignIn, btnGuestMode;
    private TextView textSignUp;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;

    private static final String GUEST_EMAIL = "individualproject2025@gmail.com";
    private static final String GUEST_PASSWORD = "Samsung2025";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();

        emailSignIn = findViewById(R.id.emailSignIn);
        passwordSignIn = findViewById(R.id.passwordSignIn);
        btnSignIn = findViewById(R.id.btnSignIn);
        btnGuestMode = findViewById(R.id.btnGuestMode);
        textSignUp = findViewById(R.id.textSignUp);
        progressBar = findViewById(R.id.progressBar);

        btnSignIn.setOnClickListener(v -> {
            String email = emailSignIn.getText().toString().trim();
            String password = passwordSignIn.getText().toString().trim();

            if (!validateInputs(email, password)) return;
            loginUser(email, password);
        });

        btnGuestMode.setOnClickListener(v -> {
            // Sign in with guest account
            loginUser(GUEST_EMAIL, GUEST_PASSWORD);
        });

        textSignUp.setOnClickListener(v -> startActivity(new Intent(SignInActivity.this, SignUpActivity.class)));
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(SignInActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(SignInActivity.this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loginUser(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        btnSignIn.setEnabled(false);
        btnGuestMode.setEnabled(false); // Disable guest button during sign-in

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        btnSignIn.setEnabled(true);
                        btnGuestMode.setEnabled(true);

                        if (task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this,
                                    email.equals(GUEST_EMAIL) ? "Signed in as Guest" : "Sign In Successful!",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(SignInActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignInActivity.this,
                                    "Sign In Failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}