package com.fp_5487.instagramclone;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private List<Post> postList;
    private Context context;
    private DatabaseReference userRef;

    public PostAdapter(Context context, List<Post> postList) {
        this.postList = postList;
        this.context = context;
    }

    @Override
    public PostViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PostViewHolder holder, int position) {
        Post post = postList.get(holder.getAdapterPosition()); // Use the adapter position here

        // Bind data
        holder.descriptionTextView.setText(post.getDescription());

        if (post.getImageBitmap() != null) {
            holder.postImageView.setVisibility(View.VISIBLE);
            holder.postImageView.setImageBitmap(post.getImageBitmap());
        }

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(post.getUserId());
        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.usernameTextView.setText(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.usernameTextView.setText("Username");
            }
        });

        userRef.child("profilePic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profilePictureBase64 = snapshot.getValue(String.class);
                if (profilePictureBase64 != null) {
                    byte[] decodedBytes = Base64.decode(profilePictureBase64, Base64.DEFAULT);
                    Bitmap profileBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    holder.profileImageView.setImageBitmap(profileBitmap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.profileImageView.setImageResource(R.drawable.profile);
            }
        });

        if (post.getLocation() != null && !post.getLocation().isEmpty()) {
            holder.locationTextView.setText(post.getLocation());
            holder.locationTextView.setVisibility(View.VISIBLE);
        } else {
            holder.locationTextView.setVisibility(View.GONE);
        }

        holder.likesCountTextView.setText(
                context.getString(R.string.likes_count, post.getLikesCount())
        );

        if (post.getTimestamp() != null) {
            Date date = new Date(Long.parseLong(post.getTimestamp()));
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String formattedDate = outputFormat.format(date);
            holder.timestamp.setText(formattedDate);
        } else {
            holder.timestamp.setVisibility(View.GONE);
        }

        holder.likeIcon.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition(); // Fetch the current adapter position dynamically
            if (currentPosition == RecyclerView.NO_POSITION) return; // Safeguard for invalid positions

            Post currentPost = postList.get(currentPosition);
            String userId = currentPost.getUserId();
            DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(currentPost.getId());
            DatabaseReference likesRef = postRef.child("likes");

            likesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean alreadyLiked = snapshot.exists() && snapshot.getValue(Boolean.class);

                    if (alreadyLiked) {
                        likesRef.child(userId).setValue(false);
                        currentPost.setLikesCount(currentPost.getLikesCount() - 1);
                        holder.likeIcon.setImageResource(R.drawable.ic_heart_filled);
                    } else {
                        likesRef.child(userId).setValue(true);
                        currentPost.setLikesCount(currentPost.getLikesCount() + 1);
                        holder.likeIcon.setImageResource(R.drawable.ic_like);
                    }

                    // Update likes count in Firebase
                    postRef.child("likesCount").setValue(currentPost.getLikesCount());

                    // Notify the adapter about the change
                    notifyItemChanged(currentPosition);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error
                }
            });
        });

        holder.commentIcon.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("postId", post.getId());
            context.startActivity(intent);
        });
    }




    @Override
    public int getItemCount() {

        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView, postImageView, likeIcon, commentIcon;
        TextView usernameTextView, locationTextView, likesCountTextView, descriptionTextView, timestamp;

        public PostViewHolder(View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            postImageView = itemView.findViewById(R.id.postImageView);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            commentIcon = itemView.findViewById(R.id.commentIcon);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            likesCountTextView = itemView.findViewById(R.id.likesCountTextView);
            descriptionTextView = itemView.findViewById(R.id.postDescriptionTextView);
            timestamp = itemView.findViewById(R.id.timestampTextView);
        }
    }
}
