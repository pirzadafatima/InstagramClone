package com.fp_5487.instagramclone;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
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
    private DrawerLayout drawerLayout;
    private Button editProfileButton;
    private ProfileFragmentAdapter fragmentAdapter;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize views
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);
        drawerLayout = view.findViewById(R.id.drawer_layout);
        editProfileButton = view.findViewById(R.id.editProfileButton);

        mAuth = FirebaseAuth.getInstance();

        // Set up ViewPager and Adapter
        fragmentAdapter = new ProfileFragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    setTabIcon(tab, R.drawable.ic_grid);
                    break;
                case 1:
                    setTabIcon(tab, R.drawable.ic_tagged);
                    break;
            }
        }).attach();

        // Edit Profile Button Listener
        editProfileButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_editProfile, new EditProfileFragment())
                    .addToBackStack(null)
                    .commit();
        });

        // Menu Icon Listener
        ImageView menuIcon = view.findViewById(R.id.menuIcon);
        menuIcon.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Navigation Drawer Listener
        NavigationView navigationView = view.findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_saved) {
                Toast.makeText(getContext(), "Saved clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_discover_people) {
                Toast.makeText(getContext(), "Discover People clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_dark_mode) {
                Toast.makeText(getContext(), "Dark Mode clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_sign_out) {
                signOutUser();
            }
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        return view;
    }

    private void setTabIcon(TabLayout.Tab tab, int drawableId) {
        Drawable icon = ContextCompat.getDrawable(requireContext(), drawableId);
        int iconSize = getResources().getDimensionPixelSize(R.dimen.tab_icon_size);
        if (icon != null) {
            icon.setBounds(0, 0, iconSize, iconSize);
        }
        tab.setIcon(icon);
    }

    private void signOutUser() {
        mAuth.signOut();
        Toast.makeText(getContext(), "Signed out successfully!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getContext(), Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
