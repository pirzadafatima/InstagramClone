package com.fp_5487.instagramclone;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.CameraSelector;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import androidx.camera.view.PreviewView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PhotoFragment extends Fragment {

    private static final String TAG = "PhotoFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageCapture imageCapture;
    private PreviewView previewView;
    private ImageView imageView; // ImageView to display the photo
    private LinearLayout buttonLayout; // The layout for Cancel and Next buttons
    private ExecutorService cameraExecutor;
    private File photoFile;

    public PhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);

        Button takePhotoButton = rootView.findViewById(R.id.take_photo_button);
        previewView = rootView.findViewById(R.id.preview_view); // Use PreviewView for the camera preview
        imageView = rootView.findViewById(R.id.image_view_photo); // ImageView to display the captured image
        buttonLayout = rootView.findViewById(R.id.button_layout); // Find the LinearLayout for buttons

        // Initialize the camera executor for background tasks
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check and request permissions
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.CAMERA}, 101);
        } else {
            startCamera();
        }

        takePhotoButton.setOnClickListener(v -> takePhoto());

        return rootView;
    }

    private void startCamera() {
        // Get the camera provider and set up camera preview and capture
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Preview configuration
                Preview preview = new Preview.Builder().build();
                // Set surface provider to PreviewView
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Camera selector (using back camera)
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // ImageCapture configuration
                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(1080, 1080)) // 1:1 aspect ratio
                        .build();

                // Unbind all previous use cases
                cameraProvider.unbindAll();

                // Bind new use cases (Preview + ImageCapture)
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void takePhoto() {
        // Create an image file to store the photo
        photoFile = createImageFile();

        if (photoFile != null) {
            // Set output options for image capture
            ImageCapture.OutputFileOptions outputOptions =
                    new ImageCapture.OutputFileOptions.Builder(photoFile).build();

            // Capture the image
            imageCapture.takePicture(
                    outputOptions,
                    cameraExecutor,
                    new ImageCapture.OnImageSavedCallback() {
                        @Override
                        public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                            String msg = "Photo saved to " + photoFile.getAbsolutePath();
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
                                // Hide the PreviewView and show the ImageView with the captured photo
                                previewView.setVisibility(View.GONE);
                                imageView.setVisibility(View.VISIBLE);
                                // Display the captured photo in the ImageView
                                displayCapturedImage();
                                // Show the "Cancel" and "Next" buttons
                                buttonLayout.setVisibility(View.VISIBLE); // Show buttons





                                // Convert the Bitmap to Base64 and store it in SharedPreferences
                                Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                                Bitmap croppedBitmap = cropAndResizeBitmap(imageBitmap, 500);
                                String encodedImage = encodeBitmapToBase64(croppedBitmap);

                                SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("image_prefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("imageBase64", encodedImage);
                                editor.apply(); // Commit the changes

                                // Optionally, open the next activity
                                Intent intent = new Intent(requireContext(), PostActivity.class);
                                startActivity(intent);
                            });
                        }

                        @Override
                        public void onError(ImageCaptureException exception) {
                            Log.e(TAG, "Photo capture failed: " + exception.getMessage());
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireContext(), "Capture failed", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
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
    private void displayCapturedImage() {
        // Decode the image file into a Bitmap and set it to the ImageView
        Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
        imageView.setImageBitmap(imageBitmap);
    }

    private File createImageFile() {
        // Create an image file to save the photo
        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = requireContext().getExternalFilesDir(null);
        File image = new File(storageDir, imageFileName + ".jpg");
        return image;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Shutdown the camera executor
        cameraExecutor.shutdown();
    }
}
