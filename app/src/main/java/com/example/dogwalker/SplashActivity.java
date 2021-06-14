package com.example.dogwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new CountDownTimer(3000, 1000) {
            @Override public void onTick(long millisUntilFinished) { }
            @Override public void onFinish() {
                if (currentUser == null) { // No user found
                    Intent intent = new Intent(SplashActivity.this, SignUpLoginActivity.class);
                    intent.putExtra("login_mode", false);
                    startActivity(intent);
                } else if (currentUser.isEmailVerified()) { // User found and verified
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } else { // User found but not verified
                    Intent intent = new Intent(SplashActivity.this, SignUpLoginActivity.class);
                    intent.putExtra("login_mode", true);
                    startActivity(intent);
                }
                finish();
            }
        }.start();
    }
}