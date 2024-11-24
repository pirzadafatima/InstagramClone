package com.fp_5487.instagramclone;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateOfBirthEditFragment extends Fragment {

    private EditText dobEditText;
    private Button editDobButton;
    private DatabaseReference userReference;
    private Calendar calendar;

    private boolean isEditing = false;  // Track if the user is editing or not

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout
        View view = inflater.inflate(R.layout.fragment_date_of_birth_edit, container, false);

        // Initialize views
        dobEditText = view.findViewById(R.id.dob_edit_text);
        editDobButton = view.findViewById(R.id.edit_dob_button);
        calendar = Calendar.getInstance();

        // Get Firebase reference
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        // Load current DoB from Firebase
        loadDateOfBirth();

        // Handle Edit button click
        editDobButton.setOnClickListener(v -> {
            if (isEditing) {
                // Save the DoB to Firebase
                String updatedDob = dobEditText.getText().toString().trim();
                if (!updatedDob.isEmpty()) {
                    saveDateOfBirth(updatedDob);
                } else {
                    Toast.makeText(getContext(), "Please select a valid Date of Birth", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Enable DatePicker dialog
                openDatePicker();
            }
        });

        return view;
    }

    private void loadDateOfBirth() {
        // Fetch DoB from Firebase
        userReference.child("dateOfBirth").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String dob = task.getResult().getValue(String.class);
                if (dob != null) {
                    dobEditText.setText(dob); // Display the saved DoB
                }
            } else {
                // Handle error if required
                Toast.makeText(getContext(), "Failed to load Date of Birth", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveDateOfBirth(String dob) {
        Log.d("DateOfBirth", "Saving Date of Birth: " + dob); // Log to confirm what’s being saved

        userReference.child("dateOfBirth").setValue(dob).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("DateOfBirth", "Date of Birth saved successfully.");
                Toast.makeText(getContext(), "Date of Birth updated successfully", Toast.LENGTH_SHORT).show();
                dobEditText.setFocusable(false);
                dobEditText.setInputType(InputType.TYPE_NULL);
                editDobButton.setText("Edit");
                isEditing = false;  // Toggle back to "Edit" state
            } else {
                Log.d("DateOfBirth", "Failed to save Date of Birth: " + task.getException().getMessage());
                Toast.makeText(getContext(), "Failed to update Date of Birth", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDatePicker() {
        // Initialize DatePicker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String selectedDate = dateFormat.format(calendar.getTime());
                    dobEditText.setText(selectedDate); // Update EditText with the selected date
                    editDobButton.setText("Save"); // Change button to 'Save' when editing
                    isEditing = true;  // Toggle to "Save" state
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
}
