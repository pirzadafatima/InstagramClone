package com.fp_5487.instagramclone;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AccountInfoFragment extends Fragment {
    private Switch privacyToggle;
    private Button deleteAccountButton;
    private DatabaseReference userReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_info, container, false);

        // Initialize Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Initialize views
        privacyToggle = view.findViewById(R.id.privacy_toggle);
        deleteAccountButton = view.findViewById(R.id.delete_account_button);

        // Load account privacy status
        loadPrivacyStatus();

        // Handle privacy toggle changes
        privacyToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String accessType = isChecked ? "PRIVATE" : "PUBLIC";
            updatePrivacyStatus(accessType);
        });

        // Handle delete account button click
        deleteAccountButton.setOnClickListener(v -> confirmDeleteAccount());

        return view;
    }

    private void loadPrivacyStatus() {
        userReference.child("accountAccessType").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().getValue() != null) {
                String accessType = task.getResult().getValue(String.class);
                boolean isPrivate = "PRIVATE".equalsIgnoreCase(accessType);
                privacyToggle.setChecked(isPrivate);
            } else {
                Toast.makeText(getContext(), "Failed to load privacy settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePrivacyStatus(String accessType) {
        userReference.child("accountAccessType").setValue(accessType).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String message = "PRIVATE".equals(accessType) ? "Account is now private" : "Account is now public";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to update privacy settings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action is irreversible.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Remove user data from Firebase
        userReference.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Delete user authentication
                FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();


                        // Redirect to SignUp page after account deletion
                        Intent intent = new Intent(getContext(), Signup.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear previous activities
                        startActivity(intent);

                        // Finish the current activity to prevent user from returning to the current screen
                        requireActivity().finish();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete account authentication", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Failed to delete account data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
