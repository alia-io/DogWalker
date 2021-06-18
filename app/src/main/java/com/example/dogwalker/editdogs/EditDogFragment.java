package com.example.dogwalker.editdogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class EditDogFragment extends DialogFragment {

    public static EditDogFragment newInstance(int layout) {
        EditDogFragment fragment = new EditDogFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int layout = args.getInt("layout");

        // TODO: getTag() == "add_dog" || "edit_dog"

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layout, null);

        return builder.setView(view).create();
    }
}
