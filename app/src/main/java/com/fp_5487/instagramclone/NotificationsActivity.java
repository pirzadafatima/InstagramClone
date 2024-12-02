package com.fp_5487.instagramclone;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private List<Notification> notifications;
    private ImageView backbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        recyclerView = findViewById(R.id.notifications_recycler_view);

        // Sample notifications data
        notifications = new ArrayList<>();
        notifications.add(new Notification("hafsahzulqarnain", "Follow Request", "5 minutes ago", true));
        notifications.add(new Notification("martini_rond", "liked your post", "2 hours ago", false));
        notifications.add(new Notification("maxjacobson", "commented on your post", "1 day ago", false));

        // Set up RecyclerView
        adapter = new NotificationsAdapter(this, notifications);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Back Button Logic
        backbtn = findViewById(R.id.back_button);
        backbtn.setOnClickListener(v -> finish()); // Close the activity and return to the previous screen
    }
}
