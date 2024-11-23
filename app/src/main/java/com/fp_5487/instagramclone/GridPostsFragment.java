package com.fp_5487.instagramclone;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class GridPostsFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private GridPostAdapter postAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid_posts, container, false);

        // Initialize RecyclerView
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Set up RecyclerView with GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3); // 3 columns
        postsRecyclerView.setLayoutManager(gridLayoutManager);

        // Add spacing between grid items
        int spacing = 8; // Define spacing in pixels
        postsRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = spacing / 2;
                outRect.right = spacing / 2;
            }
        });

        // Load sample data
        List<Integer> postImages = new ArrayList<>();
        postImages.add(R.drawable.sample_image);
        postImages.add(R.drawable.sample_image);
        postImages.add(R.drawable.sample_image);

        // Attach adapter
        postAdapter = new GridPostAdapter(getContext(), postImages);
        postsRecyclerView.setAdapter(postAdapter);

        return view;
    }
}
