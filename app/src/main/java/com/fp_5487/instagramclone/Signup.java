package com.fp_5487.instagramclone;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Signup extends AppCompatActivity {

    private EditText etPhoneNumber, etFullName, etUsername, etPassword;
    private Button btnSignUp;

    // Firebase Database reference
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Initialize UI components
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        // Set up button listener for signup
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createUsersNodeIfNeeded(); // Ensure the "Users" node exists
            }
        });
    }

    private void createUsersNodeIfNeeded() {
        // Check if the "Users" node already exists
        databaseReference.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create the "Users" node if it does not exist
                    databaseReference.child("Users").setValue(null);
                }
                // Proceed to register the user
                registerUser();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Signup.this, "Failed to access database: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerUser() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(phoneNumber)) {
            etPhoneNumber.setError("Phone number is required");
            return;
        }
        if (TextUtils.isEmpty(fullName)) {
            etFullName.setError("Full name is required");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Username is required");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Save user data to Firebase Realtime Database
        String userId = databaseReference.child("Users").push().getKey(); // Generate a unique key for each user
        HashMap<String, String> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("phoneNumber", phoneNumber);
        userMap.put("fullName", fullName);
        userMap.put("username", username);
        userMap.put("password", password); // Note: Storing plaintext passwords is NOT recommended

        if (userId != null) {
            databaseReference.child("Users").child(userId).setValue(userMap)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(Signup.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity or navigate to another screen
                        } else {
                            Toast.makeText(Signup.this, "Signup failed. Try again!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
