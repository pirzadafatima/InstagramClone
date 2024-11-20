package com.fp_5487.instagramclone;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FollowRequestsFragment extends Fragment {

    private RecyclerView followRequestsRecyclerView;
    private FollowRequestAdapter adapter;
    private List<FollowRequest> followRequestList;
    private DatabaseReference databaseReference; // Reference to the Realtime Database
    private Context context;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_follow_requests, container, false);

        // Initialize RecyclerView
        followRequestsRecyclerView = view.findViewById(R.id.followRequestsRecyclerView);
        followRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Firebase Realtime Database and data list
        databaseReference = FirebaseDatabase.getInstance().getReference("followRequests");
        followRequestList = new ArrayList<>();

        // Load data from Firebase Realtime Database
        loadFollowRequests();

        // Set up adapter
        adapter = new FollowRequestAdapter(followRequestList, context);
        followRequestsRecyclerView.setAdapter(adapter);

        return view;
    }

    private void loadFollowRequests() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                followRequestList.clear(); // Clear existing data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FollowRequest followRequest = dataSnapshot.getValue(FollowRequest.class);
                    followRequestList.add(followRequest);
                }
                adapter.notifyDataSetChanged(); // Notify adapter of data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FollowRequestsFragment", "Error loading data: " + error.getMessage());
            }
        });
    }
}
