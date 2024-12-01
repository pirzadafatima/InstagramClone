package com.fp_5487.instagramclone;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private final List<Comment> comments;
    private final DatabaseReference commentsRef;

    public CommentsAdapter(List<Comment> comments, DatabaseReference commentsRef) {
        this.comments = comments;
        this.commentsRef = commentsRef;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.usernameTextView.setText(comment.getUsername());
        holder.commentTextView.setText(comment.getCommentText());
        holder.likeCount.setText(String.valueOf(comment.getLikes()));

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentUserId = auth.getCurrentUser().getUid();

        if (comment.getLikedBy() != null && comment.getLikedBy().containsKey(currentUserId)) {
            holder.likeView.setImageResource(R.drawable.ic_heart_filled);
        } else {
            holder.likeView.setImageResource(R.drawable.ic_like);
        }

        holder.likeView.setOnClickListener(v -> {
            if (comment.getLikedBy() != null && comment.getLikedBy().containsKey(currentUserId)) {
                // Unlike
                commentsRef.child(comment.getId()).child("likedBy").child(currentUserId).removeValue();
                commentsRef.child(comment.getId()).child("likes").setValue(comment.getLikes() - 1);
                comment.setLikes(comment.getLikes() - 1);
                comment.getLikedBy().remove(currentUserId);
            } else {
                // Like
                commentsRef.child(comment.getId()).child("likedBy").child(currentUserId).setValue(true);
                commentsRef.child(comment.getId()).child("likes").setValue(comment.getLikes() + 1);
                comment.setLikes(comment.getLikes() + 1);

                if (comment.getLikedBy() == null) {
                    comment.setLikedBy(new HashMap<>()); // Initialize if null
                }
                comment.getLikedBy().put(currentUserId, true);
            }

            // Notify the adapter of the change
            notifyItemChanged(position);
        });

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(comment.getUserId());
        userRef.child("profilePic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String base64Image = snapshot.getValue(String.class);
                if (base64Image != null && !base64Image.isEmpty()) {
                    Log.d("Profile Pic", base64Image);
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    Bitmap circularBitmap = getCircularBitmapWithWhiteBackground(bitmap);
                    holder.profilePicView.setImageBitmap(circularBitmap);
                } else {
                    holder.profilePicView.setImageResource(R.drawable.profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.profilePicView.setImageResource(R.drawable.profile);
            }
        });
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public TextView commentTextView;
        public ImageView likeView, profilePicView;
        public TextView likeCount;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.comment_user_name);
            commentTextView = itemView.findViewById(R.id.comment_text);
            likeView = itemView.findViewById(R.id.comment_like_icon);
            likeCount = itemView.findViewById(R.id.likeCount);
            profilePicView = itemView.findViewById(R.id.comment_profile_pic);
        }
    }

    private Bitmap decodeBase64ToBitmap(String base64Image) {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
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

        // Draw a white circle
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);

        // Draw the circular bitmap
        BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(shader);

        float left = (width - size) / 2f;
        float top = (height - size) / 2f;
        RectF rect = new RectF(0, 0, size, size);
        canvas.drawOval(rect, paint);

        return output;
    }
}
