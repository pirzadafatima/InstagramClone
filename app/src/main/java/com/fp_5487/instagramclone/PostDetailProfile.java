package com.fp_5487.instagramclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PostDetailProfile extends Fragment {

    private Post post;
    private Button back_Button;
    private ImageView profileImageView, postImageView, likeIcon, commentIcon, shareIcon;
    private TextView usernameTextView, locationTextView, likesCountTextView, descriptionTextView, timestampTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        // Retrieve post object from arguments
        if (getArguments() != null) {
            post = (Post) getArguments().getSerializable("post");
        }

        // Bind views
        profileImageView = view.findViewById(R.id.profileImageView);
        postImageView = view.findViewById(R.id.postImageView);
        likeIcon = view.findViewById(R.id.likeIcon);
        commentIcon = view.findViewById(R.id.commentIcon);
        shareIcon = view.findViewById(R.id.shareIcon);
        usernameTextView = view.findViewById(R.id.usernameTextView);
        locationTextView = view.findViewById(R.id.locationTextView);
        likesCountTextView = view.findViewById(R.id.likesCountTextView);
        descriptionTextView = view.findViewById(R.id.postDescriptionTextView);
        timestampTextView = view.findViewById(R.id.timestampTextView);
        back_Button = view.findViewById(R.id.back_action);
        commentIcon = view.findViewById(R.id.commentIcon);


        if (post != null) {
            // Set the post details in the UI
            fetchUsername(post.getUserId()); // Fetch username from the Users table
            descriptionTextView.setText(post.getDescription());

            if (post.getImageBitmap() != null) {
                postImageView.setImageBitmap(post.getImageBitmap());
                postImageView.setVisibility(View.VISIBLE);
            } else {
                postImageView.setVisibility(View.GONE);
            }

            if (post.getLocation() != null && !post.getLocation().isEmpty()) {
                locationTextView.setText(post.getLocation());
                locationTextView.setVisibility(View.VISIBLE);
            } else {
                locationTextView.setVisibility(View.GONE);
            }

            // Set likes count
            likesCountTextView.setText(String.format("%d Likes", post.getLikesCount()));

            // Set timestamp
            if (post.getTimestamp() != null) {
                Date date = new Date(Long.parseLong(post.getTimestamp()));
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String formattedDate = outputFormat.format(date);
                timestampTextView.setText(formattedDate);
            } else {
                timestampTextView.setVisibility(View.GONE);
            }

            commentIcon.setOnClickListener(v -> {
                Intent intent = new Intent(getContext(), CommentsActivity.class);
                intent.putExtra("postId", post.getId());
                startActivity(intent);
            });
            back_Button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Go back to the previous fragment or activity
                    Log.d("Postdetail", String.valueOf(requireActivity()));

                    ProfileFragment postDetailFragment = new ProfileFragment();
                    FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                    transaction.replace(R.id.postDetails, postDetailFragment);
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            });

            // Load the profile image
            loadProfileImage(post.getUserId());

            // Handle like and share actions
            handleLikes();
            handleShare();

        }

        return view;
    }

    private void fetchUsername(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String username = snapshot.getValue(String.class);
                if (username != null) {
                    usernameTextView.setText(username);  // Set the username in the UI
                } else {
                    usernameTextView.setText("Unknown User");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                usernameTextView.setText("Unknown User");
            }
        });
    }

    private void loadProfileImage(String userId) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.child("profilePic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String base64Image = snapshot.getValue(String.class);
                if (base64Image != null && !base64Image.isEmpty()) {
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    Bitmap circularBitmap = getCircularBitmapWithWhiteBackground(bitmap);
                    profileImageView.setImageBitmap(circularBitmap);
                } else {
                    profileImageView.setImageResource(R.drawable.profile);  // Default profile image
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                profileImageView.setImageResource(R.drawable.profile);  // Default profile image
            }
        });
    }

    private void handleLikes() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts").child(post.getId());

        if (post.getLikes() != null && post.getLikes().containsKey(currentUserId)) {
            likeIcon.setImageResource(R.drawable.ic_heart_filled);  // Filled heart if liked
        } else {
            likeIcon.setImageResource(R.drawable.ic_like);  // Empty heart if not liked
        }

        likeIcon.setOnClickListener(v -> {
            if (post.getLikes() != null && post.getLikes().containsKey(currentUserId)) {
                // Unlike
                postRef.child("likes").child(currentUserId).removeValue();
                postRef.child("likesCount").setValue(post.getLikesCount() - 1);
                post.setLikesCount(post.getLikesCount() - 1);
                post.getLikes().remove(currentUserId);
            } else {
                // Like
                postRef.child("likes").child(currentUserId).setValue(true);
                postRef.child("likesCount").setValue(post.getLikesCount() + 1);
                post.setLikesCount(post.getLikesCount() + 1);

                if (post.getLikes() == null) {
                    post.setLikes(new HashMap<>());
                }
                post.getLikes().put(currentUserId, true);
            }

            likesCountTextView.setText(String.format("%d Likes", post.getLikesCount()));
            likeIcon.setImageResource(post.getLikes().containsKey(currentUserId) ? R.drawable.ic_heart_filled : R.drawable.ic_like);
        });
    }

    private void handleShare() {
        shareIcon.setOnClickListener(v -> {
            // Handle post save/un-save action
            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userSavedPostsRef = FirebaseDatabase.getInstance()
                    .getReference("savedPosts")
                    .child(currentUserId)
                    .child(post.getId());

            userSavedPostsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Post is already saved, unsave it
                        userSavedPostsRef.removeValue()
                                .addOnSuccessListener(aVoid -> shareIcon.setImageResource(R.drawable.ic_bookmark));
                    } else {
                        // Save the post
                        userSavedPostsRef.setValue(true)
                                .addOnSuccessListener(aVoid -> shareIcon.setImageResource(R.drawable.saved));
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle error
                }
            });
        });
    }

    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
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
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        return output;
    }
}
