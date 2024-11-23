package com.fp_5487.instagramclone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class PersonalDetailsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_personal_details, container, false);
        // Back button logic
        ImageView backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            // Navigate back to the Edit Profile page
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // Done button logic
        TextView doneButton = view.findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> {
            // Perform save actions here if needed
            // Then navigate back
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        return view;
    }

}
