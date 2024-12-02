package com.fp_5487.instagramclone;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;  // Using Picasso for image loading

public class ProfileLogin extends AppCompatActivity {
    private FirebaseAuth auth;
    private DatabaseReference databaseReference;
    private TextView usernameTextView;
    private ImageView profileImageView;
    private Button loginBtn;
    private TextView signupBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_login);

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        usernameTextView = findViewById(R.id.username);
        profileImageView = findViewById(R.id.profileImage);
        loginBtn = findViewById(R.id.btnLogin);
        signupBtn = findViewById(R.id.tvSignup);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Your Information...");
        progressDialog.setCancelable(false);

        if (currentUser != null) {
            progressDialog.show();
            databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser.getUid());
            databaseReference.get().addOnCompleteListener(this, new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        progressDialog.dismiss();
                        String username = task.getResult().child("username").getValue(String.class);
                        String base64Image = task.getResult().child("profilePic").getValue(String.class);

                        Log.d("Profile Pic", base64Image);
                        if (base64Image != null && !base64Image.isEmpty()) {
                            Bitmap bitmap = decodeBase64ToBitmap(base64Image);
                            Bitmap circularBitmap = getCircularBitmapWithWhiteBackground(bitmap);
                            profileImageView.setImageBitmap(circularBitmap);
                        } else {
                            profileImageView.setImageResource(R.drawable.profile); // Default image
                        }
                        assert username != null;
                        Log.d("Tag", username);
                        usernameTextView.setText(username);
//                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
//                            Picasso.get().load(profileImageUrl).into(profileImageView);
//                        } else {
//                            profileImageView.setImageResource(R.drawable.profile);
//                        }
                    }
                }
            });
        }

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileLogin.this, Signup.class);
                startActivity(intent);
            }
        });

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileLogin.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
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
