package com.fp_5487.instagramclone;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

public class HomeActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private FragmentAdapter fragmentAdapter;

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

    }

// Helper function to scale the icon size
        private void setTabIcon(TabLayout.Tab tab, int drawableId) {
            Drawable icon = getResources().getDrawable(drawableId, null);

            // Set the size of the icon (adjust the width and height as needed)
            int iconSize = getResources().getDimensionPixelSize(R.dimen.tab_icon_size);
            icon.setBounds(0, 0, iconSize, iconSize);

            tab.setIcon(icon);
        }

}