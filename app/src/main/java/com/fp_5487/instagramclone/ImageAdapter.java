package com.fp_5487.instagramclone;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private final List<String> imagePaths; // List of image file paths
    private final ImageView selectedImageView;

    public ImageAdapter(List<String> imagePaths, ImageView selectedImageView) {
        this.imagePaths = imagePaths;
        this.selectedImageView = selectedImageView;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);

        // Calculate dynamic thumbnail size
        int screenWidth = holder.imageView.getContext().getResources().getDisplayMetrics().widthPixels;
        int columnCount = 4; // Same as GridLayoutManager span count
        int imageSize = screenWidth / columnCount;

        // Log the image path for debugging
        Log.d("ImageAdapter", "Loading image from path: " + imagePath);

        Picasso.get()
                .load("file://" + imagePath)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        Log.d("Picasso", "Image loaded successfully");
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e("Picasso", "Error loading image: " + e.getMessage());
                    }
                });


        // Set click listener to display the selected image in the large view
        holder.itemView.setOnClickListener(v -> Picasso.get()
                .load("file://" + imagePath)
                .into(selectedImageView));

        // Optionally, you can set content descriptions for accessibility purposes
        String imageFileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
        holder.imageView.setContentDescription("Image: " + imageFileName);
    }

    @Override
    public int getItemCount() {
        Log.d("ImageAdapter", "Item count: " + imagePaths.size());
        return imagePaths.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
        }
    }
}
