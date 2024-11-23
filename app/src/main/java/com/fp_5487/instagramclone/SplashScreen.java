package com.fp_5487.instagramclone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);

        ImageView logo = findViewById(R.id.iv_logo);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        logo.startAnimation(fadeIn);

        // Delay for splash screen (3 seconds)
        new Handler().postDelayed(() -> {
            auth = FirebaseAuth.getInstance();
            FirebaseUser currentUser = auth.getCurrentUser();

            if (currentUser != null) {
                // User is logged in - Navigate to Profile-Based Login
                Intent intent = new Intent(SplashScreen.this, ProfileLogin.class);
                startActivity(intent);
            } else {
                // No user logged in - Navigate to Signup Page
                Intent intent = new Intent(SplashScreen.this, Login.class);
                startActivity(intent);
            }
            finish(); // Close Splash Screen
        }, 3000);
    }
}

