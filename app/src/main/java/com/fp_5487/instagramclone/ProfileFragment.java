package com.fp_5487.instagramclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private DrawerLayout drawerLayout;
    private Button editProfileButton;
    private ProfileFragmentAdapter fragmentAdapter;
    private FirebaseAuth mAuth;
    private TextView fullname, userName, userbio, post_count, follower_count, following_count;
    private ImageView profilePic;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d("ProfileFragment", "onCreateView started");

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        try {
            // Initialize views
            tabLayout = view.findViewById(R.id.tabLayout);
            viewPager = view.findViewById(R.id.viewPager);
            drawerLayout = view.findViewById(R.id.drawer_layout);
            editProfileButton = view.findViewById(R.id.editProfileButton);
            fullname = view.findViewById(R.id.FullName);
            userName = view.findViewById(R.id.profileUsername);
            userbio = view.findViewById(R.id.bio);
            post_count = view.findViewById(R.id.postsCount);
            follower_count = view.findViewById(R.id.followersCount);
            following_count = view.findViewById(R.id.followingCount);
            profilePic = view.findViewById(R.id.profileImage);
            mAuth = FirebaseAuth.getInstance();

            // Check if views are null
            if (tabLayout == null || viewPager == null || drawerLayout == null) {
                Log.e("ProfileFragment", "One or more views are null. Check XML layout.");
                return view;
            }

            // Set up ViewPager and Adapter
            fragmentAdapter = new ProfileFragmentAdapter(this);
            viewPager.setAdapter(fragmentAdapter);
            Log.d("ProfileFragment", "ViewPager adapter set");

            // Link TabLayout with ViewPager2
            new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
                try {
                    switch (position) {
                        case 0:
                            setTabIcon(tab, R.drawable.ic_grid);
                            break;
                        case 1:
                            setTabIcon(tab, R.drawable.ic_tagged);
                            break;
                    }
                } catch (Exception e) {
                    Log.e("ProfileFragment", "Error setting TabLayout icon", e);
                }
            }).attach();

            // Edit Profile Button Listener
            editProfileButton.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_profile, new EditProfileFragment()) // Ensure fragment_container exists
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

            fetchUserData();
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error in onCreateView", e);
        }

        return view;
    }

    private void setTabIcon(TabLayout.Tab tab, int drawableId) {
        try {
            Drawable icon = ContextCompat.getDrawable(requireContext(), drawableId);
            int iconSize = getResources().getDimensionPixelSize(R.dimen.tab_icon_size);
            if (icon != null) {
                icon.setBounds(0, 0, iconSize, iconSize);
            }
            tab.setIcon(icon);
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error setting tab icon", e);
        }
    }

    private void signOutUser() {
        try {
            mAuth.signOut();
            Toast.makeText(getContext(), "Signed out successfully!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        } catch (Exception e) {
            Log.e("ProfileFragment", "Error during sign out", e);
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }


    private Bitmap getCircularBitmapWithWhiteBackground(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Draw a white circle
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Draw the circular bitmap
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        float left = (width - size) / 2f;
        float top = (height - size) / 2f;
        RectF rect = new RectF(0, 0, size, size);
        canvas.drawOval(rect, paint);

        return output;
    }
    private void fetchUserData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");

            databaseReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    try {
                        if (dataSnapshot.exists()) {
                            // Fetch user data
                            String name = dataSnapshot.child("fullName").getValue(String.class);
                            fullname.setText(name != null ? name : "No Name");

                            String username = dataSnapshot.child("username").getValue(String.class);
                            userName.setText(username != null ? username : "No Username");

                            String bio = dataSnapshot.child("bio").getValue(String.class);
                            userbio.setText(bio != null ? bio : "No Bio");

                            Integer followersCount = dataSnapshot.child("followers").getValue(Integer.class);
                            Integer followingCount = dataSnapshot.child("following").getValue(Integer.class);
                            Integer postsCount = dataSnapshot.child("posts").getValue(Integer.class);

                            // Update counts with null checks
                            follower_count.setText(String.valueOf(followersCount != null ? followersCount : 0));
                            following_count.setText(String.valueOf(followingCount != null ? followingCount : 0));
                            post_count.setText(String.valueOf(postsCount != null ? postsCount : 0));

                            // Load profile image
                            String base64Image = dataSnapshot.child("profilePic").getValue(String.class);
                            Log.d("Profile Pic", base64Image);
                            if (base64Image != null && !base64Image.isEmpty()) {
                                Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                                Bitmap circularBitmap = getCircularBitmapWithWhiteBackground(bitmap);
                                profilePic.setImageBitmap(circularBitmap);
                            } else {
                                profilePic.setImageResource(R.drawable.ic_profile); // Default image
                            }
                        } else {
                            Toast.makeText(getContext(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error fetching user data", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ProfileFragment", "Database error: " + databaseError.getMessage());
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

}
