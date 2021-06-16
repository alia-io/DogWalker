package com.example.dogwalker.setupprofile;

import com.example.dogwalker.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class Fragment1 extends Fragment {

    private FragmentTracker fragmentTracker;
    private View view;
    private CheckBox dogOwner;
    private CheckBox dogWalker;
    private EditText dogOwnerYears;
    private EditText dogOwnerMonths;
    private EditText dogOwnerDays;
    private EditText dogWalkerYears;
    private EditText dogWalkerMonths;
    private EditText dogWalkerDays;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_setupprofile_1, container, false);

            dogOwner = view.findViewById(R.id.dog_owner);
            dogWalker = view.findViewById(R.id.dog_walker);

            dogOwnerYears = view.findViewById(R.id.owner_years);
            dogOwnerMonths = view.findViewById(R.id.owner_months);
            dogOwnerDays = view.findViewById(R.id.owner_days);
            dogWalkerYears = view.findViewById(R.id.walker_years);
            dogWalkerMonths = view.findViewById(R.id.walker_months);
            dogWalkerDays = view.findViewById(R.id.walker_days);

            TextView dogOwnerText = view.findViewById(R.id.owner_instruction);
            TextView dogWalkerText = view.findViewById(R.id.walker_instruction);
            LinearLayout dogOwnerTime = view.findViewById(R.id.dog_owner_time);
            LinearLayout dogWalkerTime = view.findViewById(R.id.dog_walker_time);

            dogOwner.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    dogOwnerText.setVisibility(View.VISIBLE);
                    dogOwnerTime.setVisibility(View.VISIBLE);
                } else {
                    dogOwnerText.setVisibility(View.GONE);
                    dogOwnerTime.setVisibility(View.GONE);
                }
            });

            dogWalker.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    dogWalkerText.setVisibility(View.VISIBLE);
                    dogWalkerTime.setVisibility(View.VISIBLE);
                }
                else {
                    dogWalkerText.setVisibility(View.GONE);
                    dogWalkerTime.setVisibility(View.GONE);
                }
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

    @Override
    public void onDetach() {
        super.onDetach();

        int ownerYears = 0;
        int ownerMonths = 0;
        int ownerDays = 0;
        int walkerYears = 0;
        int walkerMonths = 0;
        int walkerDays = 0;

        if (!dogOwnerYears.getText().toString().equals("")) ownerYears = Integer.parseInt(dogOwnerYears.getText().toString());
        if (!dogOwnerMonths.getText().toString().equals("")) ownerMonths = Integer.parseInt(dogOwnerMonths.getText().toString());
        if (!dogOwnerDays.getText().toString().equals("")) ownerDays = Integer.parseInt(dogOwnerDays.getText().toString());
        if (!dogWalkerYears.getText().toString().equals("")) walkerYears = Integer.parseInt(dogWalkerYears.getText().toString());
        if (!dogWalkerMonths.getText().toString().equals("")) walkerMonths = Integer.parseInt(dogWalkerMonths.getText().toString());
        if (!dogWalkerDays.getText().toString().equals("")) walkerDays = Integer.parseInt(dogWalkerDays.getText().toString());

        fragmentTracker.saveFragment1(dogOwner.isChecked(), dogWalker.isChecked(), ownerYears, ownerMonths, ownerDays, walkerYears, walkerMonths, walkerDays);
    }
}
