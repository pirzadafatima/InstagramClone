package com.fp_5487.instagramclone;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class AddPostFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;


    public AddPostFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_post, container, false);

        // Initialize TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tabLayout_post);
        viewPager = view.findViewById(R.id.viewPager_post);

        // Set up the ViewPager with a FragmentStateAdapter
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new LibraryFragment(); // Show the LibraryFragment on the first tab
                    case 1:
                        return new PhotoFragment(); // Show PhotoFragment on the second tab
                    case 2:
                        return new ReelsAddFragment(); // Show ReelsFragment on the third tab
                    default:
                        return new LibraryFragment();
                }
            }

            @Override
            public int getItemCount() {
                return 3; // Number of tabs
            }
        };

        viewPager.setAdapter(adapter);

        // Link TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Library");
                    break;
                case 1:
                    tab.setText("Photo");
                    break;
                case 2:
                    tab.setText("Reels");
                    break;
            }
        }).attach();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Additional setup here
    }
}