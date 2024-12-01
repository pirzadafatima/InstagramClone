package com.fp_5487.instagramclone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FragmentAdapter fragmentAdapter;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Set up ViewPager2 with the fragments
        fragmentAdapter = new FragmentAdapter(this);
        viewPager.setAdapter(fragmentAdapter);

        // Link the TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    setTabIcon(tab, R.drawable.ic_home);  // Home icon
                    break;
                case 1:
                    setTabIcon(tab, R.drawable.ic_explore);  // Explore icon
                    break;
                case 2:
                    setTabIcon(tab, R.drawable.ic_add_post);  // Add Post icon
                    break;
                case 3:
                    setTabIcon(tab, R.drawable.ic_reel);  // Reels icon
                    break;
                case 4:
                    setTabIcon(tab, R.drawable.ic_profile);  // Profile icon
                    break;
            }
        }).attach();

        fetchUserData();
    }

// Helper function to scale the icon size
        private void setTabIcon(TabLayout.Tab tab, int drawableId) {
            Drawable icon = getResources().getDrawable(drawableId, null);

            // Set the size of the icon (adjust the width and height as needed)
            int iconSize = getResources().getDimensionPixelSize(R.dimen.tab_icon_size);
            icon.setBounds(0, 0, iconSize, iconSize);

            tab.setIcon(icon);
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
                            // Load profile image
                            String base64Image = dataSnapshot.child("profilePic").getValue(String.class);
                            if (base64Image != null && !base64Image.isEmpty()) {
                                Log.d("Base64", base64Image);
                                Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                                Bitmap circularBitmap = getCircularBitmapWithWhiteBackground(bitmap);

                                // Update the Profile Tab's icon dynamically
                                updateProfileTabIcon(circularBitmap);
                            } else {
                                // Set default profile icon
                                updateProfileTabIcon(null);
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("HomeActivity", "Error fetching user data", e);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("HomeActivity", "Database error: " + databaseError.getMessage());
                    Toast.makeText(HomeActivity.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(HomeActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap original, int width, int height) {
        return Bitmap.createScaledBitmap(original, width, height, false);
    }

    private void updateProfileTabIcon(Bitmap profileBitmap) {
        // Ensure this is being run on the main UI thread
        Log.d("HomeActivity", "Updating profile tab icon...");
        runOnUiThread(() -> {
            if (tabLayout != null) {
                TabLayout.Tab profileTab = tabLayout.getTabAt(4); // Profile Tab index
                if (profileTab != null) {
                    if (profileBitmap != null) {
                        // Resize the bitmap if necessary (optional)
                        Bitmap resizedBitmap = resizeBitmap(profileBitmap, 100, 100); // Resize to 100x100 or whatever size you prefer

                        // Check if the Bitmap is not null
                        Log.d("HomeActivity", "Bitmap is valid, creating Drawable...");
                        Drawable drawable = new BitmapDrawable(getResources(), resizedBitmap);
                        profileTab.setIcon(drawable);
                        Log.d("HomeActivity", "Profile tab icon updated with Bitmap.");
                    } else {
                        profileTab.setIcon(R.drawable.ic_profile);
                        Log.d("HomeActivity", "Set default profile icon.");
                    }
                } else {
                    Log.d("HomeActivity", "Profile tab is null.");
                }
            }
        });
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
}