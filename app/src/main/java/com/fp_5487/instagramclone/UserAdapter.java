package com.fp_5487.instagramclone;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList; // List of users to display
    private Context context;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout for each user
        View view = LayoutInflater.from(context).inflate(R.layout.user_item, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = userList.get(position);

        holder.usernameTextView.setText(user.getUsername());
        holder.nameTextView.setText(user.getFullName());
        holder.followersCountTextView.setText(String.format("Followers: %d", user.getFollowers()));

        //Picasso.get()
                //.load(user.getProfilePic())
                //.placeholder(R.drawable.ic_profile)// Add error handling
                //.error(R.drawable.error)
                //.into(holder.profileImageView);
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof PostActivity) {
                // Cast to PostActivity and call addUserToTaggedUsers()
                ((PostActivity) context).addUserToTaggedUsers(user);
            } else {
                Log.e("UserAdapter", "Context is not an instance of PostActivity.");
            }
        });

    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    // ViewHolder class to hold references to views for each user item
    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView nameTextView;
        TextView followersCountTextView;
        ImageView profileImageView;

        public UserViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.username);
            nameTextView = itemView.findViewById(R.id.name);
            followersCountTextView = itemView.findViewById(R.id.followers_count);
            //profileImageView = itemView.findViewById(R.id.image_profile);
        }
    }

    public void updateUserList(List<User> newUserList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return userList.size();
            }

            @Override
            public int getNewListSize() {
                return newUserList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return userList.get(oldItemPosition).getUsername()
                        .equals(newUserList.get(newItemPosition).getUsername());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return userList.get(oldItemPosition).equals(newUserList.get(newItemPosition));
            }
        });

        userList.clear();
        userList.addAll(newUserList);
        diffResult.dispatchUpdatesTo(this);
    }

}
