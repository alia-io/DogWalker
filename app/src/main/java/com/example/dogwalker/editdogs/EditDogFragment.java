package com.example.dogwalker.editdogs;

import com.example.dogwalker.R;

import android.app.ActionBar;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class EditDogFragment extends DialogFragment {

    private View view;
    private int height = 0;

    public static EditDogFragment newInstance(int layout, int height) {
        EditDogFragment fragment = new EditDogFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putInt("height", height);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.height = height;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int layout = args.getInt("layout");
        height = args.getInt("height");

        // TODO: getTag() == "add_dog" || "edit_dog"

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(layout, null);

        Spinner spinner = view.findViewById(R.id.walk_length_units);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.dog_walk_length_units, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // button onClick
        //RadioGroup rg = findViewById(R.id.rg);
        //rg.getCheckedRadioButtonId();
        //switch to find checked rb

        return builder.setView(view).create();
    }
}
