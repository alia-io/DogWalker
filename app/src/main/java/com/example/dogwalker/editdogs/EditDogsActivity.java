package com.example.dogwalker.editdogs;

import com.example.dogwalker.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class EditDogsActivity extends AppCompatActivity {

    ConstraintLayout activityLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dogs);
        setSupportActionBar(findViewById(R.id.toolbar));

        activityLayout = findViewById(R.id.layout_parent);
    }

    public void addDog(View view) {
        Log.d("height", "view height = " + activityLayout.getHeight());
        Log.d("height", "subtract height = " + getSupportActionBar().getHeight());
        EditDogFragment.newInstance(R.layout.fragment_edit_dog, activityLayout.getHeight() - (getSupportActionBar().getHeight() * 3))
                .show(getSupportFragmentManager(), "add_dog");
    }
}