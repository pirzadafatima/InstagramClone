package com.fp_5487.instagramclone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class EditProfileFragment extends Fragment {

    private EditText nameField, usernameField, bioField, websiteField;
    private ImageView profileImage, backArrow;
    private TextView personalInfo, editPicture;
    private Spinner gender;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        // Initialize views
        backArrow = view.findViewById(R.id.iv_back_arrow);
        profileImage = view.findViewById(R.id.profile_image);
        editPicture = view.findViewById(R.id.edit_picture_text);
        nameField = view.findViewById(R.id.name_field);
        usernameField = view.findViewById(R.id.username_field);
        bioField = view.findViewById(R.id.bio_field);
        gender = view.findViewById(R.id.gender_spinner);
        personalInfo = view.findViewById(R.id.personal_info);
        websiteField = view.findViewById(R.id.website_field);

        // Set up gender spinner
        setupGenderSpinner();

        // Fetch user data from Firebase and populate fields
        fetchUserData();

        // Set a click listener to navigate to PersonalDetailsFragment
        personalInfo.setOnClickListener(v -> {
            PersonalDetailsFragment fragment = new PersonalDetailsFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_editProfile, fragment) // Replace with your container ID
                    .addToBackStack(null) // Allows back navigation
                    .commit();
        });

        backArrow.setOnClickListener(v -> {
            saveProfileChanges();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Set a click listener for editing the profile picture
        editPicture.setOnClickListener(v -> openImagePicker());

        return view;
    }

    // Fetch user data from Firebase and populate the fields
    private void fetchUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid(); // Get current user UID
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve user data from Firebase
                        String name = dataSnapshot.child("fullName").getValue(String.class);
                        String username = dataSnapshot.child("username").getValue(String.class);
                        String website = dataSnapshot.child("website").getValue(String.class);
                        String bio = dataSnapshot.child("bio").getValue(String.class);
                        String genderValue = dataSnapshot.child("gender").getValue(String.class);

                        // Set data in EditText fields
                        nameField.setText(name);
                        usernameField.setText(username);
                        bioField.setText(bio);
                        websiteField.setText(website);


                        // Set default gender to "Male" if gender is empty or null
                        if (genderValue == null || genderValue.isEmpty()) {
                            genderValue = "Male"; // Set to Male if not set
                        }

                        // Set the gender spinner selection based on the fetched gender
                        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) gender.getAdapter();
                        int spinnerPosition = adapter.getPosition(genderValue);
                        gender.setSelection(spinnerPosition);

                        // Optionally, if the user has a profile image URL, load it into the profile image
                        String profileImageUrl = dataSnapshot.child("profileImageUrl").getValue(String.class);
                        if (profileImageUrl != null) {
                            Glide.with(requireContext())
                                    .load(profileImageUrl)
                                    .into(profileImage); // Use Glide to load the image into ImageView
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Function to open the image picker (using an intent to pick a picture)
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 1); // Request code 1 for image picker
    }

    // Handle image selection result
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == getActivity().RESULT_OK && requestCode == 1 && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri); // Update the profile image with the selected image
            Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Setup gender spinner with options
    private void setupGenderSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_options, // Your gender options resource
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter);
    }

    private void saveProfileChanges() {
        String name = nameField.getText().toString().trim();
        String username = usernameField.getText().toString().trim();
        String bio = bioField.getText().toString().trim();
        String website = websiteField.getText().toString().trim();
        String selectedGender = gender.getSelectedItem().toString().trim();

        if (name.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Name and Username cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

            // Update user data in Firebase
            databaseReference.child("fullName").setValue(name);
            databaseReference.child("username").setValue(username);
            databaseReference.child("bio").setValue(bio);
            databaseReference.child("gender").setValue(selectedGender);
            databaseReference.child("website").setValue(website);

            Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}
