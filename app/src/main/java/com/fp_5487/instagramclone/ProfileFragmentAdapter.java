package com.fp_5487.instagramclone;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfileFragmentAdapter extends FragmentStateAdapter {

    public ProfileFragmentAdapter(@NonNull ProfileFragment fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new UserPostsFragment();
            case 1:
                return new UserTaggedPosts();
            default:
                return new UserPostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;  // Number of tabs
    }
}