package com.fp_5487.instagramclone;
import android.content.Context;


import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReelPostAdapter extends RecyclerView.Adapter<ReelPostAdapter.ViewHolder> {

    private Context context;
    private List<Post> postList;

    public ReelPostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.reel_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Post post = postList.get(position);

        holder.descriptionTextView.setText(post.getDescription());
        if (post.getVideoFile() != null) {

            Uri videoUri = Uri.fromFile(post.getVideoFile());
            holder.videoView.setVideoURI(videoUri);

            // Auto-play video
            holder.videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                holder.videoView.start();
            });
        } else {
            Log.e("ReelPostAdapter", "Video file is null for position: " + position);
        }
    }


    // Optionally, set a timestamp or other metadata here

    @Override
    public int getItemCount() {
        return postList.size();
    }

    private String saveVideoToFile(byte[] videoData) {
        // Save the video data to a file and return the file path
        // For example, save it in the app's storage and return the path
        String fileName = "post_video_" + System.currentTimeMillis() + ".mp4";
        File videoFile = new File(context.getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(videoFile)) {
            fos.write(videoData);
            return videoFile.getAbsolutePath(); // Return the path of the saved video
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView descriptionTextView;
        public VideoView videoView;

        public ViewHolder(View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.description);
            videoView = itemView.findViewById(R.id.videoView);
        }
    }

    // Helper method to play the video from file using VideoView
    private void playVideo(VideoView videoView, File videoFile) {
        if (videoFile != null) {
            Uri videoUri = Uri.fromFile(videoFile);
            videoView.setVideoURI(videoUri);
            videoView.start();
        } else {
        }
    }
}
