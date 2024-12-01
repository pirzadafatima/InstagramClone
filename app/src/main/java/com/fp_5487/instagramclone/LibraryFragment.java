package com.fp_5487.instagramclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView nextButton;
    private ImageAdapter imageAdapter;
    private List<String> imagePaths;  // List of image file paths
    private ImageView selectedImageView;
    private static final int REQUEST_PERMISSION = 1;
    private SharedPreferences sharedPreferences;
    public LibraryFragment() {
        // Required empty constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_library, container, false);

        nextButton = view.findViewById(R.id.next_button);
        selectedImageView = view.findViewById(R.id.selected_image);
        recyclerView = view.findViewById(R.id.image_recycler_view);

        // Initialize the image list and adapter
        imagePaths = new ArrayList<>();
        imageAdapter = new ImageAdapter(imagePaths, selectedImageView);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        recyclerView.setAdapter(imageAdapter);


        nextButton.setOnClickListener(v -> {
            // Get the Bitmap from the ImageView
            selectedImageView.setDrawingCacheEnabled(true); // Enable the drawing cache
            selectedImageView.buildDrawingCache(); // Build the drawing cache
            Bitmap obitmap = ((BitmapDrawable) selectedImageView.getDrawable()).getBitmap(); // Get the Bitmap

            // Encode the Bitmap to Base64
            Bitmap croppedBitmap = cropAndResizeBitmap(obitmap, 500);
            String encodedImage = encodeBitmapToBase64(croppedBitmap);

            // Save the Base64 encoded image in SharedPreferences
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("image_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("imageBase64", encodedImage);
            editor.apply(); // Commit the changes

            // Optionally, open the next activity
            Intent intent = new Intent(requireContext(), PostActivity.class);
            startActivity(intent);
        });
        // Check for permission and request if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, use READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permission granted");
                loadImagesFromGallery();  // Permission granted, proceed with loading images
            } else {
                Log.d("Permissions", "Permission not granted");
                requestPermission();
            }
        } else {
            // For Android versions below 13, use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permission granted");
                loadImagesFromGallery();  // Permission granted, proceed with loading images
            } else {
                Log.d("Permissions", "Permission not granted");
                requestPermission();
            }
        }

        return view;
    }

    // Method to request permission
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // For Android 13 and above, request READ_MEDIA_IMAGES permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_MEDIA_IMAGES)) {
                new AlertDialog.Builder(getContext())
                        .setMessage("We need access to your images to show them in the gallery.")
                        .setPositiveButton("OK", (dialog, which) -> {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
                        })
                        .setNegativeButton("Cancel", null)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_PERMISSION);
            }
        } else {
            // For Android versions below 13, request READ_EXTERNAL_STORAGE permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(getContext())
                        .setMessage("We need access to your external storage to show images from your gallery.")
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

    private void loadImagesFromGallery() {
        // Create a URI for accessing the gallery
        Uri imageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // Define columns to retrieve
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};

        // Query the content provider
        Cursor cursor = getActivity().getContentResolver().query(imageUri, projection, null, null, null);
        if (cursor != null) {
            Log.d("GalleryQuery", "Cursor count: " + cursor.getCount());
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                do {
                    String imagePath = cursor.getString(columnIndex);
                    Log.d("GalleryQuery", "Image path: " + imagePath);  // Log the path to confirm
                    imagePaths.add(imagePath);
                } while (cursor.moveToNext());
                cursor.close();

                // After images are loaded, update the adapter
                imageAdapter.notifyDataSetChanged();
            } else {
                Log.d("GalleryQuery", "No images found.");
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


    private Bitmap cropAndResizeBitmap(Bitmap bitmap, int targetSize) {
        // Determine the smallest dimension
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = Math.min(width, height);

        // Crop the image to a square (1:1 aspect ratio)
        Bitmap croppedBitmap = Bitmap.createBitmap(
                bitmap,
                (width - size) / 2, // X coordinate of the first pixel
                (height - size) / 2, // Y coordinate of the first pixel
                size, // Width of the square
                size // Height of the square
        );

        // Resize the cropped bitmap to the target size
        return Bitmap.createScaledBitmap(croppedBitmap, targetSize, targetSize, true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permissions", "Permission granted!");
                loadImagesFromGallery(); // Load images after permission is granted
            } else {
                // Permission denied, show a message to the user
                Log.d("Permissions", "Permission denied!");
                Toast.makeText(getContext(), "Permission denied to read your external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
