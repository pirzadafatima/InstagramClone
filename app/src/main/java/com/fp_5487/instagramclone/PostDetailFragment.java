package com.fp_5487.instagramclone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class PostDetailFragment extends Fragment {

    private Post selectedPost;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        if (getArguments() != null) {
            selectedPost = getArguments().getParcelable("selectedPost"); // Get the passed post
        }

        // Use selectedPost data to display post details
        // Example: Show post image and description
        ImageView postImage = view.findViewById(R.id.postImage);
        TextView postDescription = view.findViewById(R.id.postDescription);

        if (selectedPost != null) {
            postImage.setImageBitmap(selectedPost.getImageBitmap()); // Assuming you have a method to get the image
            postDescription.setText(selectedPost.getDescription()); // Set the description or other data
        }

        return view;
    }
}
