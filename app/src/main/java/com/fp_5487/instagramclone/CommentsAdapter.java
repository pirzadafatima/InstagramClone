package com.fp_5487.instagramclone;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    private List<Comment> comments;

    public CommentsAdapter(List<Comment> comments) {

        this.comments = comments;
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
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView usernameTextView;
        public TextView commentTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.comment_user_name);
            commentTextView = itemView.findViewById(R.id.comment_text);
        }
    }
}
