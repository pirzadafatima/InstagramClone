package com.fp_5487.instagramclone;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private DatabaseReference databaseReference;
    private Button actionButton;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);

        recyclerView = rootView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Get user list from arguments with a null check
        List<User> userList = getArguments() != null ? getArguments().getParcelableArrayList("userList") : new ArrayList<>();
        if (userList == null) {
            userList = new ArrayList<>();
        }

        // Initialize adapter
        userAdapter = new UserAdapter(getContext(), userList);
        recyclerView.setAdapter(userAdapter);

        // Handle item click in the UserAdapter
        userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(User user) {
                // Navigate to the ProfileFragment when a user is clicked
                navigateToProfile(user);
            }
        });

        actionButton = rootView.findViewById(R.id.btn_action);

        // Set a click listener on the button
        actionButton.setOnClickListener(v -> {
            // Destroy the current fragment when the button is clicked
            removeFragment();
        });
        return rootView;
    }

    private void navigateToProfile(User user) {
        // Create a bundle with user information
        Bundle bundle = new Bundle();
        bundle.putString("userId", user.getUsername());

        // Get the HomeActivity and replace ExploreFragment with ProfileFragment
        if (getActivity() instanceof HomeActivity) {
            HomeActivity homeActivity = (HomeActivity) getActivity();

            // Create a ProfileFragment and pass the bundle to it
            ProfileFragment profileFragment = new ProfileFragment();
            profileFragment.setArguments(bundle);

            // Replace ExploreFragment with ProfileFragment
            homeActivity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.Explore, profileFragment) // ID of the container where ExploreFragment was
                    .addToBackStack(null)  // Add to back stack so we can go back to ExploreFragment
                    .commit();
        }
    }

    private void removeFragment() {
        // Check if the fragment is attached
        if (isAdded()) {
            FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
            transaction.remove(UserListFragment.this); // Remove this fragment
            transaction.commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Optional: Cleanup any resources if necessary
    }


}

