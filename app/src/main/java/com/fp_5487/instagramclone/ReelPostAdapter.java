package com.fp_5487.instagramclone;
import android.content.Context;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ReelPostAdapter extends RecyclerView.Adapter<ReelPostAdapter.ViewHolder> {

    private Context context;
    private List<Post> postList;
    private DatabaseReference userRef;

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

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(post.getUserId());
        userRef.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                holder.nameView.setText(snapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.nameView.setText("username");
            }
        });

        userRef.child("profilePic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String base64Image = snapshot.getValue(String.class);
                if (base64Image != null && !base64Image.isEmpty()) {
                    Log.d("Profile Pic", base64Image);
                    Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                    Bitmap circularBitmap = getCircularBitmapWithWhiteBackground(bitmap);
                    holder.profileView.setImageBitmap(circularBitmap);
                } else {
                    holder.profileView.setImageResource(R.drawable.ic_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.profileView.setImageResource(R.drawable.profile);
            }
        });

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
        public TextView descriptionTextView, nameView;
        public VideoView videoView;
        public ImageView profileView;

        public ViewHolder(View itemView) {
            super(itemView);
            descriptionTextView = itemView.findViewById(R.id.description);
            videoView = itemView.findViewById(R.id.videoView);
            profileView = itemView.findViewById(R.id.profileImage);
            nameView = itemView.findViewById(R.id.profileName);

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
