package com.fp_5487.instagramclone;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.fp_5487.instagramclone.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfileFragmentAdapter fragmentAdapter;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Set up ViewPager2 with the fragments
        fragmentAdapter = new ProfileFragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);

        mAuth = FirebaseAuth.getInstance();

        // Set up the Sign Out button
        Button signOutButton = view.findViewById(R.id.signout);
        signOutButton.setOnClickListener(v -> {
            signOutUser();
        });

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    setTabIcon(tab, R.drawable.ic_grid);  // Grid icon
                    break;
                case 1:
                    setTabIcon(tab, R.drawable.ic_tagged);  // Tagged icon
                    break;
            }
        }).attach();

        return view;
    }

    // Helper function to scale the icon size
    private void setTabIcon(TabLayout.Tab tab, int drawableId) {
        Drawable icon = getResources().getDrawable(drawableId, null);

        // Set the size of the icon (adjust the width and height as needed)
        int iconSize = getResources().getDimensionPixelSize(R.dimen.tab_icon_size);
        icon.setBounds(0, 0, iconSize, iconSize);

        tab.setIcon(icon);
    }

    private void signOutUser() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Signed out successfully!", Toast.LENGTH_SHORT).show();

        // Redirect user to LoginActivity
        Intent intent = new Intent(getContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Optional: Close the current activity to prevent back navigation
        requireActivity().finish();
    }
}
