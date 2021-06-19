package com.example.dogwalker.editdogs;

import android.view.View;

import com.example.dogwalker.Dog;

public interface EditDogDialogListener {
    void onAddDogPictureButtonClick(View v);
    void setDog(Dog dog, String dogId, boolean isDogNew, boolean isProfilePictureNew);
}
