package com.fp_5487.instagramclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.SharedPreferences;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PostActivity extends AppCompatActivity {

    private ImageView postImageView;
    private EditText postDescriptionEditText;
    private Button postButton, backBtn;
    private EditText location;
    private List<String> taggedUsers;

    private DatabaseReference databaseReference;
    private DatabaseReference userRef;
    private String base64Image;
    private String currentUserId;
    private final Handler handler = new Handler();
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post);

        postImageView = findViewById(R.id.postImageView);
        postDescriptionEditText = findViewById(R.id.postDescriptionEditText);
        postButton = findViewById(R.id.postButton);
        location = findViewById(R.id.Location);
        SearchView searchView = findViewById(R.id.SearchTaggedPeople);
        searchView.setQueryHint("Search");
        backBtn = findViewById(R.id.back_action_back);
        taggedUsers = new ArrayList<>();
        // Initialize Firebase Realtime Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        // Retrieve the Base64 encoded image string from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("image_prefs", MODE_PRIVATE);
        base64Image = sharedPreferences.getString("imageBase64", null);

        if (base64Image != null) {
            Bitmap bitmap = decodeBase64ToBitmap(base64Image);
            postImageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(PostActivity.this, "Image is Null", Toast.LENGTH_SHORT).show();

        }
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        postButton.setOnClickListener(v -> {
            String description = postDescriptionEditText.getText().toString().trim();
            String loc = location.getText().toString().trim();

            if (description.isEmpty()) {
                Toast.makeText(PostActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            } else {
                // Save the post to Firebase Realtime Database with Base64 image
                savePostToDatabase(loc,description, base64Image, currentUserId);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> performSearch(query);
                handler.postDelayed(searchRunnable, 300); // Debounce with 300ms delay
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchRunnable != null) {
                    handler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> performSearch(newText);
                handler.postDelayed(searchRunnable, 300); // Debounce with 300ms delay
                return true;
            }
        });
    }

    private void savePostToDatabase(String loc,String description, String base64Image, String userID) {
        if (base64Image == null) {
            Toast.makeText(this, "No image found to upload", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique post ID and timestamp
        String postId = databaseReference.push().getKey();
        String timestamp = String.valueOf(System.currentTimeMillis());

        // Create the Post object with Base64 image data and description
        Post post = new Post(postId, userID, description, base64Image, timestamp, loc, taggedUsers);

        // Save the post to Firebase Realtime Database
        if (postId != null) {
            databaseReference.child(postId).setValue(post)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(PostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();
                        incrementUserPostCount(userID);

                        Intent intent = new Intent(PostActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish(); // Close the activity after saving
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(PostActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Helper method to decode Base64 string to Bitmap
    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private void performSearch(String query) {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        if (query.isEmpty()) {
            // If no search query, fetch the first 5 users
            userViewModel.getAllUsers().observe(this, usersList -> {
                if (usersList != null) {
                    // Limit the results to the first 5 users
                    List<User> firstFiveUsers = usersList.size() > 5 ? usersList.subList(0, 5) : usersList;
                    openUserListFragment(firstFiveUsers);

                }
            });
        } else {
            // Perform the search and get filtered users
            userViewModel.searchUsersByName(query).observe(this, usersList -> {
                if (usersList != null) {
                    openUserListFragment(usersList);
                }
            });
        }
    }

    private void openUserListFragment(List<User> usersList) {
        SearchFragmentTagged userListFragment = new SearchFragmentTagged();
        Bundle args = new Bundle();
        args.putParcelableArrayList("userList", new ArrayList<>(usersList));
        userListFragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.MyFragmentContainer, userListFragment);
        transaction.addToBackStack(null); // Add fragment to back stack
        transaction.commit();
    }

    private void incrementUserPostCount(String userID) {
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userID);

        userRef.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long currentPostCount = 0;
                if (snapshot.exists()) {
                    currentPostCount = snapshot.getValue(Long.class);
                }

                userRef.child("posts").setValue(currentPostCount + 1)
                        .addOnSuccessListener(aVoid -> Toast.makeText(PostActivity.this, "Post count updated!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(PostActivity.this, "Failed to update post count", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostActivity.this, "Error fetching post count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onFragmentRemoved() {
        // Optional: Handle UI updates or logic after the fragment is removed
        Toast.makeText(this, "Returned to PostActivity", Toast.LENGTH_SHORT).show();
    }
    public void addUserToTaggedUsers(User user) {
        // Initialize taggedUsers as an empty list


        if (!taggedUsers.contains(user.getUsername())) {
            taggedUsers.add(user.getUsername());
            Toast.makeText(this, user.getUsername() + " tagged!", Toast.LENGTH_SHORT).show();
        }
    }


}
