package com.fp_5487.instagramclone;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvSignup, forgotPassword;
    private ImageView back;

    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog; // For showing progress while logging in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        back = findViewById(R.id.back);
        mAuth = FirebaseAuth.getInstance();
        forgotPassword = findViewById(R.id.forgotPassword);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        tvSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Signup.class);
                startActivity(intent);
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Login.this);
                builder.setTitle("Reset Password");
                builder.setMessage("Enter your registered email to receive a password reset link:");

                final EditText input = new EditText(Login.this);
                input.setHint("Email");
                input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                input.setPadding(100, 20, 50, 20);
                builder.setView(input);

                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String email = input.getText().toString().trim();
                        if (TextUtils.isEmpty(email)) {
                            Toast.makeText(Login.this, "Email is required", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Login.this, "Password reset link sent to your email", Toast.LENGTH_SHORT).show();
                                } else {
                                    String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error occurred";
                                    Toast.makeText(Login.this, "Failed to send reset link: " + errorMessage, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.show();
            }
        });


        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(Login.this)
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing; dialog will dismiss automatically
                            }
                        })
                        .show();
            }
        });
    }

    private void loginUser() {
        String input = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            etUsername.setError("Email or Username is required");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        progressDialog.show();

        if (input.contains("@")) {
            // Input is an email
            loginWithEmail(input, password);
        } else {
            // Input is a username, resolve email from database
            resolveUsernameToEmail(input, new OnEmailResolvedListener() {
                @Override
                public void onEmailResolved(String email) {
                    if (email != null) {
                        loginWithEmail(email, password);
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(Login.this, "Invalid username or email", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void loginWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null && user.isEmailVerified()) {
                                // Email is verified
                                Toast.makeText(Login.this, "Login successful!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Login.this, HomeActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Email not verified
                                Toast.makeText(Login.this, "Please verify your email address.", Toast.LENGTH_LONG).show();

                                new AlertDialog.Builder(Login.this)
                                        .setTitle("Email Verification Required")
                                        .setMessage("You need to verify your email before logging in. Would you like to resend the verification email?")
                                        .setPositiveButton("Resend", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (user != null) {
                                                    user.sendEmailVerification()
                                                            .addOnCompleteListener(task1 -> {
                                                                if (task1.isSuccessful()) {
                                                                    Toast.makeText(Login.this, "Verification email resent. Please check your inbox.", Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    Toast.makeText(Login.this, "Failed to resend verification email.", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            }
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show();
                            }
                        } else {
                            // Login failed
                            Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void resolveUsernameToEmail(String username, OnEmailResolvedListener listener) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Query the 'users' node, ordering by 'username' and filtering to match the input
        usersRef.orderByChild("username").equalTo(username)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String email = userSnapshot.child("email").getValue(String.class);
                                listener.onEmailResolved(email);  // Pass the email back to the listener
                                return;  // Exit after the first match
                            }
                        } else {
                            listener.onEmailResolved(null);  // No username found, return null
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        listener.onEmailResolved(null);
                    }
                });
    }


    interface OnEmailResolvedListener {
        void onEmailResolved(String email);
    }
}
