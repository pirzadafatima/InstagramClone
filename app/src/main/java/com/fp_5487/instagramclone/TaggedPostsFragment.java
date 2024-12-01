package com.fp_5487.instagramclone;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class TaggedPostsFragment extends Fragment {

    private RecyclerView taggedRecyclerView;
    private GridPostAdapter taggedPostAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tagged_posts, container, false);

        // Initialize RecyclerView
        taggedRecyclerView = view.findViewById(R.id.taggedRecyclerView);

        // Set up RecyclerView with GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3); // 3 columns
        taggedRecyclerView.setLayoutManager(gridLayoutManager);

        // Add spacing between grid items
        int spacing = 8; // Define spacing in pixels
        taggedRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                outRect.left = spacing / 2;
                outRect.right = spacing / 2;
            }
        });


        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        // Create sample data for tagged posts (replace with your actual data source)
        List<Post> taggedImages = new ArrayList<>();

        // Set up GridLayoutManager for 3 columns
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        taggedRecyclerView.setLayoutManager(gridLayoutManager);

        // Attach adapter
        taggedPostAdapter = new GridPostAdapter(getContext(), taggedImages);
        taggedRecyclerView.setAdapter(taggedPostAdapter);
    }
}
