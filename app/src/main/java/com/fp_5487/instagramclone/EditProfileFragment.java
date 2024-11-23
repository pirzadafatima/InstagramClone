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

public class EditProfileFragment extends Fragment {

    private EditText nameField, usernameField, pronounsField, bioField;
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
        pronounsField = view.findViewById(R.id.pronouns_field);
        bioField = view.findViewById(R.id.bio_field);
        gender = view.findViewById(R.id.gender_spinner);
        personalInfo = view.findViewById(R.id.personal_info);

        // Set up gender spinner
        setupGenderSpinner();

        // Set a click listener to navigate to PersonalDetailsFragment
        personalInfo.setOnClickListener(v -> {
            PersonalDetailsFragment fragment = new PersonalDetailsFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_editProfile, fragment) // Replace with your container ID
                    .addToBackStack(null) // Allows back navigation
                    .commit();
        });

        backArrow.setOnClickListener(v -> {
            PersonalDetailsFragment fragment = new PersonalDetailsFragment();
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Set a click listener for editing the profile picture
        editPicture.setOnClickListener(v -> openImagePicker());

        return view;
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
            // Get the image URI and set it as the new profile picture
            Uri imageUri = data.getData();
            profileImage.setImageURI(imageUri);
            Toast.makeText(requireContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    // Setup gender spinner
    private void setupGenderSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(adapter);
    }
}
