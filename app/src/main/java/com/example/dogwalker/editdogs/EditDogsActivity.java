package com.example.dogwalker.editdogs;

import com.example.dogwalker.R;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class EditDogsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dogs);
    }

    public void addDog(View view) {
        EditDogFragment.newInstance(R.layout.fragment_edit_dog).show(getSupportFragmentManager(), "add_dog");
    }
}