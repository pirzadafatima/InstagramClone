package com.fp_5487.instagramclone;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private ImageView sendButton, backButton;
    private DatabaseReference commentsRef;
    private ArrayList<Comment> commentsList;
    private CommentsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_add_comment);

        // Initialize views

        commentsRecyclerView = findViewById(R.id.comments_recycler_view);
        commentInput = findViewById(R.id.comment_input);
        sendButton = findViewById(R.id.send_button);
        backButton = findViewById(R.id.back_button);

        String postId = getIntent().getStringExtra("postId");
        commentsRef = FirebaseDatabase.getInstance().getReference("comments").child(postId);

        commentsList = new ArrayList<>();
        adapter = new CommentsAdapter(commentsList, commentsRef);

        // Setup RecyclerView
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(adapter);

        // Load comments
        loadComments();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // Send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                postComment(postId);
            }
        });
    }

    private void loadComments() {
        commentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                commentsList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Comment comment = dataSnapshot.getValue(Comment.class);
                    commentsList.add(comment);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this, "Failed to load comments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void postComment(String postId) {
        String commentText = commentInput.getText().toString().trim();

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("username")) {
                    String username = snapshot.child("username").getValue(String.class);
                    String commentId = commentsRef.push().getKey();

                    Comment comment = new Comment(
                            commentId,
                            postId,
                            userId,
                            username,
                            commentText,
                            0
                    );

                    if (commentId != null) {
                        commentsRef.child(commentId).setValue(comment)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(CommentsActivity.this, "Comment posted successfully!", Toast.LENGTH_SHORT).show();
                                    commentInput.setText("");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(CommentsActivity.this, "Failed to create post", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    Toast.makeText(CommentsActivity.this, "Failed to retrieve username", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CommentsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
