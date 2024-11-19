package com.fp_5487.instagramclone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class FollowRequestAdapter extends RecyclerView.Adapter<FollowRequestAdapter.ViewHolder> {

    private final List<FollowRequest> followRequestList;
    private final Context context;

    public FollowRequestAdapter(List<FollowRequest> followRequestList, Context context) {
        this.followRequestList = followRequestList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_follow_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FollowRequest followRequest = followRequestList.get(position);

        // Bind data to views
        holder.usernameTextView.setText(followRequest.getUsername());
        holder.fullNameTextView.setText(followRequest.getFullName());

        // Load profile image using Glide (ensure the image URL is valid)
        Glide.with(context)
                .load(followRequest.getProfileImageUrl())
                .placeholder(R.drawable.ic_profile) // Fallback image
                .into(holder.profileImageView);

        // Set up button click listeners
        holder.confirmButton.setOnClickListener(v -> {
            // Handle confirm button click
            // Example: Remove the request from the list
            handleConfirmRequest(followRequest);
        });

        holder.deleteButton.setOnClickListener(v -> {
            // Handle delete button click
            // Example: Remove the request from the list
            handleDeleteRequest(followRequest);
        });
    }

    @Override
    public int getItemCount() {
        return followRequestList.size();
    }

    private void handleConfirmRequest(FollowRequest followRequest) {
        // Add logic to confirm the follow request
        // Example: Add user to followers list and remove the request
        FirebaseDatabase.getInstance().getReference("followers")
                .child(followRequest.getUsername()) // Target user
                .setValue(true); // Mark as confirmed

        FirebaseDatabase.getInstance().getReference("followRequests")
                .child(followRequest.getUsername()) // Remove from follow requests
                .removeValue();

        // Update the RecyclerView
        followRequestList.remove(followRequest);
        notifyDataSetChanged();
    }

    private void handleDeleteRequest(FollowRequest followRequest) {
        // Add logic to delete the follow request
        FirebaseDatabase.getInstance().getReference("followRequests")
                .child(followRequest.getUsername()) // Remove from follow requests
                .removeValue();

        // Update the RecyclerView
        followRequestList.remove(followRequest);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView profileImageView;
        private final TextView usernameTextView;
        private final TextView fullNameTextView;
        private final Button confirmButton;
        private final Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImage);
            usernameTextView = itemView.findViewById(R.id.username);
            fullNameTextView = itemView.findViewById(R.id.fullName);
            confirmButton = itemView.findViewById(R.id.confirmButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
