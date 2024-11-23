package com.fp_5487.instagramclone;

import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private List<String> videoPaths;
    private VideoSelectionListener selectionListener;

    public VideoAdapter(List<String> videoPaths, VideoSelectionListener selectionListener) {
        this.videoPaths = videoPaths;
        this.selectionListener = selectionListener;
    }

    @Override
    public VideoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(VideoViewHolder holder, int position) {
        String videoPath = videoPaths.get(position);

        // Optionally, load a thumbnail for the video
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
        holder.thumbnailView.setImageBitmap(thumbnail);

        holder.itemView.setOnClickListener(v -> {
            if (selectionListener != null) {
                selectionListener.onVideoSelected(position); // Pass selected position
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoPaths.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailView;

        public VideoViewHolder(View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.video_thumbnail);
        }
    }

    public interface VideoSelectionListener {
        void onVideoSelected(int position);
    }
}

