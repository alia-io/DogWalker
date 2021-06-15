package com.example.dogwalker.setupprofile;

import com.example.dogwalker.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

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
    private TextView dogOwnerText;
    private TextView dogWalkerText;
    private LinearLayout dogOwnerTime;
    private LinearLayout dogWalkerTime;
    private EditText dogOwnerYears;
    private EditText dogOwnerMonths;
    private EditText dogOwnerDays;
    private EditText dogWalkerYears;
    private EditText dogWalkerMonths;
    private EditText dogWalkerDays;
    private LinearLayout parentLayout;
    private LinearLayout dogOwnerLayout;
    private LinearLayout dogWalkerLayout;
    //private ConstraintLayout parentLayout;
    //private ConstraintSet constraintSet;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (view == null) {
            view = inflater.inflate(R.layout.fragment_set_up_profile_1, container, false);

            parentId = R.id.fragment_parent;
            dogWalkerId = R.id.dog_walker;
            dogWalkerTimeId = R.id.dog_walker_time;

            parentLayout = view.findViewById(parentId);
            dogOwnerLayout = view.findViewById(R.id.dog_owner_layout);
            dogWalkerLayout = view.findViewById(R.id.dog_walker_layout);

            dogOwner = view.findViewById(R.id.dog_owner);
            dogWalker = view.findViewById(dogWalkerId);
            dogOwnerText = view.findViewById(R.id.owner_instruction);
            dogWalkerText = view.findViewById(R.id.walker_instruction);
            dogOwnerTime = view.findViewById(R.id.dog_owner_time);
            dogWalkerTime = view.findViewById(dogWalkerTimeId);

            dogOwnerYears = view.findViewById(R.id.owner_years);
            dogOwnerMonths = view.findViewById(R.id.owner_months);
            dogOwnerDays = view.findViewById(R.id.owner_days);
            dogWalkerYears = view.findViewById(R.id.walker_years);
            dogWalkerMonths = view.findViewById(R.id.walker_months);
            dogWalkerDays = view.findViewById(R.id.walker_days);

            //constraintSet = new ConstraintSet();
            //constraintSet.clone(parentLayout);

            dogOwner.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    dogOwnerText.setVisibility(View.VISIBLE);
                    dogOwnerTime.setVisibility(View.VISIBLE);
                    //parentLayout.setWeightSum(parentLayout.getWeightSum() + 1f);

                    /*constraintSet.connect(dogWalkerId, ConstraintSet.TOP, R.id.line_75, ConstraintSet.TOP);
                    constraintSet.connect(dogWalkerId, ConstraintSet.BOTTOM, R.id.line_85, ConstraintSet.BOTTOM);
                    constraintSet.connect(dogWalkerTimeId, ConstraintSet.TOP, R.id.line_90, ConstraintSet.TOP);
                    constraintSet.connect(dogWalkerTimeId, ConstraintSet.BOTTOM, parentId, ConstraintSet.BOTTOM);*/
                } else {
                    dogOwnerText.setVisibility(View.GONE);
                    dogOwnerTime.setVisibility(View.GONE);
                    /*constraintSet.connect(dogWalkerId, ConstraintSet.TOP, R.id.line_60, ConstraintSet.TOP);
                    constraintSet.connect(dogWalkerId, ConstraintSet.BOTTOM, R.id.line_70, ConstraintSet.BOTTOM);
                    constraintSet.connect(dogWalkerTimeId, ConstraintSet.TOP, R.id.line_75, ConstraintSet.TOP);
                    constraintSet.connect(dogWalkerTimeId, ConstraintSet.BOTTOM, R.id.line_85, ConstraintSet.BOTTOM);*/
                }
                //constraintSet.applyTo(parentLayout);
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
        fragmentTracker.saveFragment1(dogOwner.isChecked(), dogWalker.isChecked(),
                Integer.parseInt(dogOwnerYears.getText().toString()), Integer.parseInt(dogOwnerMonths.getText().toString()),
                Integer.parseInt(dogOwnerDays.getText().toString()), Integer.parseInt(dogWalkerYears.getText().toString()),
                Integer.parseInt(dogWalkerMonths.getText().toString()), Integer.parseInt(dogWalkerDays.getText().toString()));
    }
}
