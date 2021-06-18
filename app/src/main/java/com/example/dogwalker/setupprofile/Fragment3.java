package com.example.dogwalker.setupprofile;

import com.example.dogwalker.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class Fragment3 extends Fragment {

    private FragmentTracker fragmentTracker;
    private View view;
    private EditText aboutMe;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_setupprofile_3, container, false);
            aboutMe = view.findViewById(R.id.about_me);
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

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentTracker.saveFragment3(aboutMe.getText().toString());
    }
}
