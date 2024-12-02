package com.fp_5487.instagramclone;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class TaggedPostsFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private GridPostAdapter postAdapter;
    private DatabaseReference databaseReference;
    List<Post> postList;
    private DataSnapshot lastVisiblePostSnapshot;
    private AlertDialog loadingDialog;
    private String currentUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tagged_posts, container, false);
        fetchCurrentUsername();

        postsRecyclerView = view.findViewById(R.id.taggedRecyclerView);
        postsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Set up RecyclerView with GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3); // 3 columns
        postsRecyclerView.setLayoutManager(gridLayoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        // Add spacing between grid item
        postList = new ArrayList<>();


        // Attach adapter to RecyclerView
        postAdapter = new GridPostAdapter(getContext(), postList);
        postsRecyclerView.setAdapter(postAdapter);

        return view;
    }

    private void fetchCurrentUsername() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(currentUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUsername = snapshot.child("username").getValue(String.class);
                    Log.d("TaggedPostsFragment", "Current username: " + currentUsername);
                    fetchInitialPosts();
                } else {
                    Log.e("TaggedPostsFragment", "User profile not found!");
                    Toast.makeText(getContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TaggedPostsFragment", "Error fetching user profile: " + error.getMessage());
                Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchInitialPosts() {
        if (currentUsername == null || currentUsername.isEmpty()) {
            Log.e("TaggedPostsFragment", "Current username is null or empty!");
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                postList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    if (post != null && post.getTaggedUsers() != null) {
                        List<String> taggedUsers = post.getTaggedUsers(); // Assuming this returns a List<String>
                        if (taggedUsers.contains(currentUsername)) {
                            String base64Image = post.getImageBase64();
                            if (base64Image != null && !base64Image.isEmpty()) {
                                Bitmap imageBitmap = decodeBase64ToBitmap(base64Image);
                                post.setImageBitmap(imageBitmap); // Convert base64 to Bitmap
                            }
                            postList.add(post);
                        }
                    }
                }

                // Update RecyclerView
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TaggedPostsFragment", "Error fetching posts: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
            }
        });
    }



    private Bitmap decodeBase64ToBitmap(String base64Image) {
        try {
            byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
