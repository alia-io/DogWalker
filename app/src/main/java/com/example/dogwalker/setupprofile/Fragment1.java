package com.example.dogwalker.setupprofile;

import com.example.dogwalker.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

public class Fragment1 extends Fragment {

    private FragmentTracker fragmentTracker;
    private View view;
    private int parentId;
    private int dogWalkerId;
    private int dogWalkerTimeId;
    private CheckBox dogOwner;
    private CheckBox dogWalker;
    private LinearLayout dogOwnerTime;
    private LinearLayout dogWalkerTime;
    private NumberPicker dogOwnerYears;
    private NumberPicker dogOwnerMonths;
    private NumberPicker dogOwnerDays;
    private NumberPicker dogWalkerYears;
    private NumberPicker dogWalkerMonths;
    private NumberPicker dogWalkerDays;
    private ConstraintLayout parentLayout;
    private ConstraintSet constraintSet;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_set_up_profile_1, container, false);

            parentId = R.id.fragment_parent;
            dogWalkerId = R.id.dog_walker;
            dogWalkerTimeId = R.id.dog_walker_time;

            parentLayout = view.findViewById(parentId);
            dogOwner = view.findViewById(R.id.dog_owner);
            dogWalker = view.findViewById(dogWalkerId);
            dogOwnerTime = view.findViewById(R.id.dog_owner_time);
            dogWalkerTime = view.findViewById(dogWalkerTimeId);

            dogOwnerYears = view.findViewById(R.id.owner_years);
            dogOwnerMonths = view.findViewById(R.id.owner_months);
            dogOwnerDays = view.findViewById(R.id.owner_days);
            dogWalkerYears = view.findViewById(R.id.walker_years);
            dogWalkerMonths = view.findViewById(R.id.walker_months);
            dogWalkerDays = view.findViewById(R.id.walker_days);

            dogOwnerYears.setMinValue(0);
            dogOwnerYears.setMaxValue(99);
            dogOwnerMonths.setMinValue(0);
            dogOwnerMonths.setMaxValue(11);
            dogOwnerDays.setMinValue(0);
            dogOwnerDays.setMaxValue(30);

            dogWalkerYears.setMinValue(0);
            dogWalkerYears.setMaxValue(99);
            dogWalkerMonths.setMinValue(0);
            dogWalkerMonths.setMaxValue(11);
            dogWalkerDays.setMinValue(0);
            dogWalkerDays.setMaxValue(30);

            constraintSet = new ConstraintSet();
            constraintSet.clone(parentLayout);

            dogOwner.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    dogOwnerTime.setVisibility(View.VISIBLE);
                    constraintSet.connect(dogWalkerId, ConstraintSet.TOP, R.id.line_75, ConstraintSet.TOP);
                    constraintSet.connect(dogWalkerId, ConstraintSet.BOTTOM, R.id.line_85, ConstraintSet.BOTTOM);
                    constraintSet.connect(dogWalkerTimeId, ConstraintSet.TOP, R.id.line_90, ConstraintSet.TOP);
                    constraintSet.connect(dogWalkerTimeId, ConstraintSet.BOTTOM, parentId, ConstraintSet.BOTTOM);
                } else {
                    dogOwnerTime.setVisibility(View.GONE);
                    constraintSet.connect(dogWalkerId, ConstraintSet.TOP, R.id.line_60, ConstraintSet.TOP);
                    constraintSet.connect(dogWalkerId, ConstraintSet.BOTTOM, R.id.line_70, ConstraintSet.BOTTOM);
                    constraintSet.connect(dogWalkerTimeId, ConstraintSet.TOP, R.id.line_75, ConstraintSet.TOP);
                    constraintSet.connect(dogWalkerTimeId, ConstraintSet.BOTTOM, R.id.line_85, ConstraintSet.BOTTOM);
                }
                constraintSet.applyTo(parentLayout);
            });

            dogWalker.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) dogWalkerTime.setVisibility(View.VISIBLE);
                else dogWalkerTime.setVisibility(View.GONE);
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
        fragmentTracker.saveFragment1(dogOwner.isChecked(), dogWalker.isChecked(),
                dogOwnerYears.getValue(), dogOwnerMonths.getValue(), dogOwnerDays.getValue(),
                dogWalkerYears.getValue(), dogWalkerMonths.getValue(), dogWalkerDays.getValue());
    }
}
