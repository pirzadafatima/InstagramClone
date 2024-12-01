package com.fp_5487.instagramclone;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Color;
import android.graphics.Shader;
import android.util.Base64;
import android.util.Log;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
                holder.usernameTextView.setText("username");
            }
        });

        userRef.child("profilePic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String base64Image = snapshot.getValue(String.class);
                if (base64Image != null && !base64Image.isEmpty()) {
                    Log.d("Profile Pic", base64Image);
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    Bitmap circularBitmap = getCircularBitmapWithWhiteBackground(bitmap);
                    holder.profileImageView.setImageBitmap(circularBitmap);
                } else {
                    holder.profileImageView.setImageResource(R.drawable.profile);
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        Log.d("userID", userId);
        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getId());

        if (post.getLikes() != null && post.getLikes().containsKey(userId)) {
            holder.likeIcon.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.likeIcon.setImageResource(R.drawable.ic_like);
        }

        holder.likeIcon.setOnClickListener(v -> {
            if (post.getLikes() != null && post.getLikes().containsKey(userId)) {
                // Unlike
                postRef.child("likes").child(userId).removeValue();
                postRef.child("likesCount").setValue(post.getLikesCount() - 1);
                post.setLikesCount(post.getLikesCount() - 1);
                post.getLikes().remove(userId);
            } else {
                // Like
                postRef.child("likes").child(userId).setValue(true);
                postRef.child("likesCount").setValue(post.getLikesCount() + 1);
                post.setLikesCount(post.getLikesCount() + 1);

                if (post.getLikes() == null) {
                    post.setLikes(new HashMap<>()); // Initialize if null
                }
                post.getLikes().put(userId, true);
            }

            // Notify the adapter of the change
            notifyItemChanged(position);
        });

        holder.commentIcon.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("postId", post.getId());
            context.startActivity(intent);
        });

        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userSavedPostsRef = FirebaseDatabase.getInstance()
                .getReference("savedPosts")
                .child(currentUserId)
                .child(post.getId());

        userSavedPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Post is saved, show filled icon
                    holder.shareIcon.setImageResource(R.drawable.saved);
                } else {
                    // Post is not saved, show unfilled icon
                    holder.shareIcon.setImageResource(R.drawable.ic_bookmark);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        holder.shareIcon.setOnClickListener(v -> {
            userSavedPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        userSavedPostsRef.removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Post unsaved successfully!", Toast.LENGTH_SHORT).show();
                                    holder.shareIcon.setImageResource(R.drawable.ic_bookmark);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to unsave post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Save the post
                        Map<String, Object> saveMap = new HashMap<>();
                        saveMap.put("postId", post.getId());
                        saveMap.put("timestamp", ServerValue.TIMESTAMP);

                        userSavedPostsRef.setValue(saveMap)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(context, "Post saved successfully!", Toast.LENGTH_SHORT).show();
                                    holder.shareIcon.setImageResource(R.drawable.saved); // Change to saved icon
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(context, "Failed to save post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


    }

    @Override
    public int getItemCount() {

        return postList.size();
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView, postImageView, likeIcon, commentIcon, shareIcon;
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
            shareIcon = itemView.findViewById(R.id.shareIcon);
        }
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
