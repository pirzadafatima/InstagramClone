package com.fp_5487.instagramclone;

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
    private ImageView sendButton;

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

        // Firebase reference (adjust for post ID if necessary)
        commentsRef = FirebaseDatabase.getInstance().getReference("comments");

        commentsList = new ArrayList<>();
        adapter = new CommentsAdapter(commentsList);

        // Setup RecyclerView
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(adapter);

        // Load comments
        loadComments();

        // Send button click listener
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
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

    private void postComment() {
        String commentText = commentInput.getText().toString().trim();

        if (TextUtils.isEmpty(commentText)) {
            Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, Object> commentMap = new HashMap<>();
        commentMap.put("username", "Anonymous"); // Replace with actual user
        commentMap.put("commentText", commentText);

        commentsRef.push().setValue(commentMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(CommentsActivity.this, "Comment added", Toast.LENGTH_SHORT).show();
                commentInput.setText("");
            } else {
                Toast.makeText(CommentsActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
