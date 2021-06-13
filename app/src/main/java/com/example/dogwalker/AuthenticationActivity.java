package com.example.dogwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthenticationActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

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
                    startActivity(new Intent(AuthenticationActivity.this, SignUpActivity.class));
                } else if (currentUser.isEmailVerified()) { // User found and verified
                    startActivity(new Intent(AuthenticationActivity.this, HomeActivity.class));
                } else { // User found but not verified
                    startActivity(new Intent(AuthenticationActivity.this, HomeActivity.class));
                }
                finish();
            }
        }.start();
    }
}