package com.fp_5487.instagramclone;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExploreFragment extends Fragment {

    public ExploreFragment() {
        // Required empty public constructor
    }

    private final Handler handler = new Handler();
    private Runnable searchRunnable;
    private RecyclerView postsRecyclerView;
    private GridPostAdapter postAdapter;

    private DatabaseReference databaseReference;
    List<Post> postList;

    private DataSnapshot lastVisiblePostSnapshot;

    private AlertDialog loadingDialog;
    private static final int POSTS_PER_BATCH = 18;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        SearchView searchView = view.findViewById(R.id.search_view);
        searchView.setQueryHint("Search");


        postsRecyclerView = view.findViewById(R.id.postsRecyclerView);

        // Set up RecyclerView for posts in grid
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3); // 3 columns
        postsRecyclerView.setLayoutManager(gridLayoutManager);

        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        // Add spacing between grid item
        postList = new ArrayList<>();


        // Attach adapter to RecyclerView
        postAdapter = new GridPostAdapter(getContext(), postList );
        postsRecyclerView.setAdapter(postAdapter);

        fetchInitialPosts();

        postsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    // Check if the user is at the bottom of the list
                    if (!recyclerView.canScrollVertically(1)) {
                        // User is at the bottom, load more posts
                        fetchMorePosts();
                    }
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


        return view;
    }

    private void performSearch(String query) {
        UserViewModel userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);

        if (query.isEmpty()) {
            // If no search query, fetch the first 5 users
            userViewModel.getAllUsers().observe(getViewLifecycleOwner(), usersList -> {
                if (usersList != null) {
                    // Limit the results to the first 5 users
                    List<User> firstFiveUsers = usersList.size() > 5 ? usersList.subList(0, 5) : usersList;
                    openUserListFragment(firstFiveUsers);
                }
            });
        } else {
            // Perform the search and get filtered users
            userViewModel.searchUsersByName(query).observe(getViewLifecycleOwner(), usersList -> {
                if (usersList != null) {
                    openUserListFragment(usersList);
                }
            });
        }
    }


    private void openUserListFragment(List<User> usersList) {
        UserListFragment userListFragment = new UserListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("userList", new ArrayList<>(usersList)); // Pass the user list
        userListFragment.setArguments(args);

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.Explore, userListFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void fetchInitialPosts() {
        Log.d("ExploreFragment", "Fetching initial posts...");
        databaseReference.limitToFirst(18) // Fetch first 18 posts
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        postList.clear(); // Clear the list to load fresh posts
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

                        // Save the last DataSnapshot for pagination
                        if (dataSnapshot.getChildren().iterator().hasNext()) {
                            lastVisiblePostSnapshot = dataSnapshot; // Store the DataSnapshot for pagination
                        } else {
                            lastVisiblePostSnapshot = null; // No more posts to load
                        }

                        postAdapter.notifyDataSetChanged(); // Notify adapter about the changes
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("ExploreFragment", "Error fetching initial posts: " + databaseError.getMessage());
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
    // Show the loading dialog
    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(R.layout.loading_dialog); // Make sure to create a layout for the loading dialog
        builder.setCancelable(false); // Prevent the dialog from being dismissed by touch or back press
        loadingDialog = builder.create();
        loadingDialog.show();
    }

    // Hide the loading dialog
    private void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private void fetchMorePosts() {
        if (lastVisiblePostSnapshot == null) {
            // No more posts to load
            return;
        }

        Log.d("ExploreFragment", "Fetching more posts...");
        showLoadingDialog();

        // Use the last visible DataSnapshot to continue fetching
        Iterable<DataSnapshot> children = lastVisiblePostSnapshot.getChildren();
        DataSnapshot lastSnapshot = null;
        for (DataSnapshot snapshot : children) {
            lastSnapshot = snapshot; // Get the last post's snapshot from the previous batch
        }

        if (lastSnapshot == null) {
            hideLoadingDialog();
            return;
        }

        // Query the database for the next batch of posts, starting after the last snapshot
        databaseReference.orderByKey() // Order by key
                .startAfter(lastSnapshot.getKey()) // Start after the last key in the last snapshot
                .limitToFirst(18) // Fetch the next batch of posts (18 more)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean hasMore = false; // Flag to check if there are more posts
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);
                            if (post != null) {
                                String base64Image = post.getImageBase64();
                                if (base64Image != null && !base64Image.isEmpty()) {
                                    Bitmap imageBitmap = decodeBase64ToBitmap(base64Image);
                                    post.setImageBitmap(imageBitmap); // Convert base64 to Bitmap
                                }
                                postList.add(post); // Add new posts to the list
                                hasMore = true;
                            }
                        }

                        if (hasMore) {
                            // Update the lastVisiblePostSnapshot to fetch the next batch
                            lastVisiblePostSnapshot = dataSnapshot; // Save the new snapshot
                        } else {
                            // No more posts to load
                            lastVisiblePostSnapshot = null;
                        }

                        postAdapter.notifyDataSetChanged(); // Update the RecyclerView
                        hideLoadingDialog();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("ExploreFragment", "Error fetching more posts: " + databaseError.getMessage());
                        Toast.makeText(getContext(), "Failed to load more posts", Toast.LENGTH_SHORT).show();
                        hideLoadingDialog();
                    }
                });
    }


}
