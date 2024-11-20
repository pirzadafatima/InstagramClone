package com.fp_5487.instagramclone;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fp_5487.instagramclone.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfileFragmentAdapter fragmentAdapter;

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
}
