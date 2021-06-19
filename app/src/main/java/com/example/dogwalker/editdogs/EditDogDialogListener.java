package com.example.dogwalker.editdogs;

import android.view.View;

import com.example.dogwalker.Dog;

public interface EditDogDialogListener {
    void onAddDogPictureButtonClick(View v);
    void setDog(String dogId, Dog dog, boolean isDogNew, boolean isProfilePictureNew);
}
