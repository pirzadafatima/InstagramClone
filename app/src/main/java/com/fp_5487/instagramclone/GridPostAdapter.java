package com.fp_5487.instagramclone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GridPostAdapter extends RecyclerView.Adapter<GridPostAdapter.GridPostViewHolder> {

    private Context context;
    private List<Integer> postImages; // Replace Integer with your post model

    public GridPostAdapter(Context context, List<Integer> postImages) {
        this.context = context;
        this.postImages = postImages;
    }

    @NonNull
    @Override
    public GridPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.profile_grid_item, parent, false);
        return new GridPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GridPostViewHolder holder, int position) {
        holder.postImage.setImageResource(postImages.get(position));
    }

    @Override
    public int getItemCount() {
        return postImages.size();
    }

    static class GridPostViewHolder extends RecyclerView.ViewHolder {
        ImageView postImage;

        public GridPostViewHolder(@NonNull View itemView) {
            super(itemView);
            postImage = itemView.findViewById(R.id.post);
        }
    }
}
