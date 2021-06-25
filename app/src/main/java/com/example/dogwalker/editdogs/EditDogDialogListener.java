package com.example.dogwalker.editdogs;

import com.example.dogwalker.Dog;

public interface EditDogDialogListener {
    void takeProfilePicture();
    void uploadProfilePicture();
    void setDog(String dogId, Dog dog, boolean isDogNew, boolean isProfilePictureNew);
}
