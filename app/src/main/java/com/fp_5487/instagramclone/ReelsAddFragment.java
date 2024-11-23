package com.fp_5487.instagramclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReelsAddFragment extends Fragment implements VideoAdapter.VideoSelectionListener{

    private RecyclerView recyclerView;
    private TextView nextButton;
    private VideoAdapter videoAdapter;
    private List<String> videoPaths;  // List of video file paths
    private VideoView selectedThumbnailView;  // VideoView to play selected video
    private static final int REQUEST_PERMISSION = 1;
    private SharedPreferences sharedPreferences;
    private int selectedVideoPosition = -1; // Store the selected position

    public ReelsAddFragment() {
        // Required empty constructor
    }


    @Override
    public void onVideoSelected(int position) {
        selectedVideoPosition = position; // Update the selected position
        Uri videoUri = Uri.fromFile(new File(videoPaths.get(position)));
        selectedThumbnailView.setVideoURI(videoUri); // Optionally preview the video
        selectedThumbnailView.start();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reels_add, container, false);

        nextButton = view.findViewById(R.id.next_button);
        selectedThumbnailView = view.findViewById(R.id.selected_video);
        recyclerView = view.findViewById(R.id.video_recycler_view);

        // Initialize the video list and adapter
        videoPaths = new ArrayList<>();
        videoAdapter = new VideoAdapter(videoPaths, this);


        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(videoAdapter);

        nextButton.setOnClickListener(v -> {
            if (selectedVideoPosition != -1 && selectedVideoPosition < videoPaths.size()) {
                File videoFile = new File(videoPaths.get(selectedVideoPosition));
                if (videoFile.exists()) {
                    Uri videoUri = Uri.fromFile(videoFile);

                    // Pass the selected video URI to the next activity
                    Intent intent = new Intent(requireContext(), ReelsPostActivity.class);
                    intent.putExtra("selectedVideoUri", videoUri.toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Selected video file not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Please select a video", Toast.LENGTH_SHORT).show();
            }
        });


        // Check for permission and request if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, use READ_MEDIA_VIDEO
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permission granted");
                loadVideosFromGallery();  // Permission granted, proceed with loading videos
            } else {
                Log.d("Permissions", "Permission not granted");
                requestPermission();
            }
        } else {
            // For Android versions below 13, use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permission granted");
                loadVideosFromGallery();  // Permission granted, proceed with loading videos
            } else {
                Log.d("Permissions", "Permission not granted");
                requestPermission();
            }
        }

        return view;
    }

    private Bitmap getVideoThumbnail(Uri videoUri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            // Setting the data source may throw an IOException, so handle it
            retriever.setDataSource(requireContext(), videoUri);
            return retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC); // Retrieve a frame at 1 second
        } catch (IllegalArgumentException e) {
            Log.e("VideoThumbnail", "Invalid video URI: " + e.getMessage());
            e.printStackTrace();
            return null;
        } catch (RuntimeException e) {
            Log.e("VideoThumbnail", "Runtime exception while extracting thumbnail: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Method to request permission
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, request READ_MEDIA_VIDEO permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_MEDIA_VIDEO)) {
                new AlertDialog.Builder(getContext())
                        .setMessage("We need access to your videos to show them in the gallery.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_PERMISSION);
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_MEDIA_VIDEO}, REQUEST_PERMISSION);
            }
        } else {
            // For Android versions below 13, request READ_EXTERNAL_STORAGE permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getContext())
                        .setMessage("We need access to your external storage to show videos from your gallery.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }
    }

    private void loadVideosFromGallery() {
        // Create a URI for accessing the gallery
        Uri videoUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        // Define columns to retrieve
        String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.DATA};

        // Query the content provider
        Cursor cursor = getActivity().getContentResolver().query(videoUri, projection, null, null, null);
        if (cursor != null) {
            Log.d("GalleryQuery", "Cursor count: " + cursor.getCount());
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                do {
                    String videoPath = cursor.getString(columnIndex);
                    Log.d("GalleryQuery", "Video path: " + videoPath);
                    videoPaths.add(videoPath);
                } while (cursor.moveToNext());
                cursor.close();

                // After videos are loaded, update the adapter
                videoAdapter.notifyDataSetChanged();
            } else {
                Log.d("GalleryQuery", "No videos found.");
            }
        } else {
            Log.d("GalleryQuery", "Cursor is null.");
        }
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream); // Compress the image
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT); // Encode as Base64
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permission granted!");
                loadVideosFromGallery(); // Load videos after permission is granted
            } else {
                // Permission denied, show a message to the user
                Log.d("Permissions", "Permission denied!");
                Toast.makeText(getContext(), "Permission denied to read your external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
