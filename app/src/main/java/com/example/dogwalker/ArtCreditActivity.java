package com.example.dogwalker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class ArtCreditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_credit);

        TextView splashScreenLogo = findViewById(R.id.splash_screen_logo);
        splashScreenLogo.setMovementMethod(LinkMovementMethod.getInstance());
    }
}