package com.example.dogwalker.setupprofile;

import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.R;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class Fragment2 extends Fragment {

    private FragmentTracker fragmentTracker;
    private View view;
    private ImageView addProfilePicture;
    private ImageView profilePicture;
    private Uri profilePictureUri;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_setupprofile_2, container, false);
            profilePicture = view.findViewById(R.id.profile_picture);
            addProfilePicture = view.findViewById(R.id.add_profile_picture);
            addProfilePicture.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(getContext(), v);
                MenuInflater menuInflater = popupMenu.getMenuInflater();
                menuInflater.inflate(R.menu.menu_picture_popup, popupMenu.getMenu());
                try {
                    popupMenu.setOnMenuItemClickListener((PopupMenu.OnMenuItemClickListener) getContext());
                } catch (ClassCastException e) {
                    throw new ClassCastException(getContext().toString() + " must implement PopupMenu.OnMenuItemClickListener");
                }
                popupMenu.show();
            });
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            fragmentTracker = (FragmentTracker) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement FragmentTracker.");
        }
    }

    public void setProfilePicturePreview(Uri imageUri) {
        profilePictureUri = imageUri;
        Picasso.get().load(profilePictureUri.toString()).transform(new CircleTransform()).into(profilePicture);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (profilePictureUri != null)
            fragmentTracker.saveFragment2(profilePictureUri);
    }
}
