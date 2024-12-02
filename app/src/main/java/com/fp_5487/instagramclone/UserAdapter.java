package com.fp_5487.instagramclone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
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
        String base64 = user.getProfilePic();
        Bitmap bitmap = decodeBase64ToBitmap(base64);
        Bitmap circular =getCircularBitmapWithWhiteBackground(bitmap);
        holder.profileImageView.setImageBitmap(circular);
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
            profileImageView = itemView.findViewById(R.id.image_profile);
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

    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = android.util.Base64.decode(base64Image, android.util.Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private Bitmap getCircularBitmapWithWhiteBackground(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        return output;
    }

}
