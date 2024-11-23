package com.fp_5487.instagramclone;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReelsFragment extends Fragment {

    private List<Post> reelList;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerViewVideo;
    private ReelPostAdapter reelPostAdapter;
    public ReelsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_reels, container, false);
        recyclerViewVideo = view.findViewById(R.id.recyclerVideos);
        recyclerViewVideo.setLayoutManager(new LinearLayoutManager(getContext()));

        reelList = new ArrayList<>();
        reelPostAdapter = new ReelPostAdapter(getContext(),reelList);
        recyclerViewVideo.setAdapter(reelPostAdapter);
        databaseReference = FirebaseDatabase.getInstance().getReference("reels");

        fetchReels();
        return view;
    }


    private void fetchReels() {
        databaseReference.orderByChild("timestamp") // Optionally order by timestamp
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        reelList.clear(); // Clear the list before adding new posts
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Post post = snapshot.getValue(Post.class);

                            if (post != null) {
                                // Decode Base64 video and save to file
                                String base64Video = post.getImageBase64();
                                if (base64Video != null && !base64Video.isEmpty()) {
                                    File videoFile = decodeBase64ToFile(base64Video);
                                    post.setVideoFile(videoFile);
                                    // Set the video file to Post
                                }

                                reelList.add(post);
                            }
                        }

                        reelPostAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to load reels", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private File decodeBase64ToFile(String base64Video) {
        try {
            byte[] decodedBytes = Base64.decode(base64Video, Base64.DEFAULT);

            File tempFile = new File(getContext().getCacheDir(), "temp_video_" + System.currentTimeMillis() + ".mp4");
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(decodedBytes);
            fos.close();

            Log.d("ReelsFragment", "Video file created at: " + tempFile.getAbsolutePath());
            return tempFile;
        } catch (IOException e) {
            Log.e("ReelsFragment", "Error decoding Base64 to file: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

}