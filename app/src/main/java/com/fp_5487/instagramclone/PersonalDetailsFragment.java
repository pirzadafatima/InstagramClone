package com.fp_5487.instagramclone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PersonalDetailsFragment extends Fragment {

    private TextView emailText, dobText;
    private ImageView contactInfo, dateOfBirth, accountSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_details, container, false);

        // Initialize UI components
        emailText = view.findViewById(R.id.email_text); // TextView to display email
        dobText = view.findViewById(R.id.dob_text); // TextView to display date of birth
        contactInfo = view.findViewById(R.id.contact_info_edit);
        dateOfBirth = view.findViewById(R.id.dob_edit);
        accountSettings = view.findViewById(R.id.account_info_edit);

        // Back button logic
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // Navigate back to the Edit Profile page
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        //Contact Button Logic
        contactInfo.setOnClickListener(v -> {
            ContactInformationFragment fragment = new ContactInformationFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_personalDetails, fragment) // Replace with your container ID
                    .addToBackStack(null) // Allows back navigation
                    .commit();
        });

        //Date of Birth Button Logic
        dateOfBirth.setOnClickListener(v -> {
            DateOfBirthEditFragment fragment = new DateOfBirthEditFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_personalDetails, fragment) // Replace with your container ID
                    .addToBackStack(null) // Allows back navigation
                    .commit();
        });

        //Account Info Button
        accountSettings.setOnClickListener(v -> {
            AccountInfoFragment fragment = new AccountInfoFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_personalDetails, fragment) // Replace with your container ID
                    .addToBackStack(null) // Allows back navigation
                    .commit();
        });

        // Done button logic
        TextView doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> {
            // Perform save actions here if needed
            // Then navigate back
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Fetch user data from Firebase
        fetchUserData();

        return view;
    }

    // Fetch user data from Firebase and display it
    private void fetchUserData() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();

        // Reference to the Firebase Realtime Database "Users" node
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Fetch user data
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get email and date of birth
                    String email = dataSnapshot.child("email").getValue(String.class);
                    String dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);

                    // Set the fetched data to the TextViews
                    emailText.setText(email);
                    dobText.setText(dateOfBirth);
                } else {
                    Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
