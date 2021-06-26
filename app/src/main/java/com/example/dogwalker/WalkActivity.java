package com.example.dogwalker;

import android.os.Bundle;

public class WalkActivity extends BackgroundAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void setNotificationIcon() { notificationIcon = findViewById(R.id.action_notification); }
}