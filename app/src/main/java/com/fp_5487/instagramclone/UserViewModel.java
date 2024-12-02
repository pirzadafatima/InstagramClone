package com.fp_5487.instagramclone;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserViewModel extends ViewModel {
    private MutableLiveData<List<User>> usersListLiveData;
    private DatabaseReference databaseReference;

    public UserViewModel() {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        usersListLiveData = new MutableLiveData<>();
    }

    // Search users by name
    public LiveData<List<User>> searchUsersByName(String query) {
        List<User> filteredUsers = new ArrayList<>();
        databaseReference.orderByChild("username")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        filteredUsers.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                filteredUsers.add(user);
                            }
                        }
                        usersListLiveData.setValue(filteredUsers); // Notify observers
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        usersListLiveData.setValue(null); // Handle errors
                    }
                });
        return usersListLiveData;
    }

    // Fetch all users (for displaying the first 5 users when no search is performed)
    public LiveData<List<User>> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        allUsers.add(user);
                    }
                }
                usersListLiveData.setValue(allUsers); // Notify observers
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                usersListLiveData.setValue(null); // Handle errors
            }
        });
        return usersListLiveData;
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        // Cleanup any Firebase listeners here
        //databaseReference.removeEventListener((ValueEventListener) this);
    }

}

