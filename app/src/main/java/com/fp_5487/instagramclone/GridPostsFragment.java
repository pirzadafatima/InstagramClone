package com.fp_5487.instagramclone;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
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

public class GridPostsFragment extends Fragment {

    private RecyclerView postsRecyclerView;
    private GridPostAdapter postAdapter;
    private DatabaseReference databaseReference;
    List<Post> postList;
    private DataSnapshot lastVisiblePostSnapshot;
    private AlertDialog loadingDialog;
    private String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grid_posts, container, false);

        // Initialize RecyclerView
        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);
        postsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        // Set up RecyclerView with GridLayoutManager
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3); // 3 columns
        postsRecyclerView.setLayoutManager(gridLayoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        // Add spacing between grid item
        postList = new ArrayList<>();


        // Attach adapter to RecyclerView
        postAdapter = new GridPostAdapter(getContext(), postList, post -> openPostDetailFragment(post));
        postsRecyclerView.setAdapter(postAdapter);

        fetchInitialPosts();

        return view;
    }

    private void fetchInitialPosts() {
        Log.d("GridPostsFragment", "Fetching initial posts...");
        databaseReference.orderByChild("userId").equalTo(currentUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        postList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);
                            if (post != null) {
                                String base64Image = post.getImageBase64();
                                if (base64Image != null && !base64Image.isEmpty()) {
                                    Bitmap imageBitmap = decodeBase64ToBitmap(base64Image);
                                    post.setImageBitmap(imageBitmap); // Convert base64 to Bitmap
                                }
                                postList.add(post);
                            }
                        }

                        // Update last visible snapshot to the last child
                        if (dataSnapshot.getChildren().iterator().hasNext()) {
                            lastVisiblePostSnapshot = dataSnapshot.getChildren().iterator().next();
                        } else {
                            lastVisiblePostSnapshot = null;
                        }

                        postAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("GridPostsFragment", "Error fetching initial posts: " + databaseError.getMessage());
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

    private void openPostDetailFragment(Post post) {
        PostDetailFragment postDetailFragment = new PostDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable("post", post); // Pass the post object to the fragment
        postDetailFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_profile, postDetailFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

}
