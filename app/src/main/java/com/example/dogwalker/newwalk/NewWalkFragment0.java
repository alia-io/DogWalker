package com.example.dogwalker.newwalk;

import com.example.dogwalker.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NewWalkFragment0 extends DialogFragment {

    private NewWalkFragmentTracker listener;
    private RadioGroup radioGroup;

    public static NewWalkFragment0 newInstance(int layout) {
        NewWalkFragment0 fragment = new NewWalkFragment0();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (NewWalkFragmentTracker) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement NewWalkFragmentTracker interface.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int layout = args.getInt("layout");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layout, null);

        radioGroup = view.findViewById(R.id.radio_group);

        return builder.setView(view).setTitle(R.string.start_walk).setIcon(R.drawable.dog_walker)
                .setPositiveButton("Continue", (dialog, which) -> onContinueClicked())
                .setNegativeButton("Cancel", (dialog, which) -> dismiss()).create();
    }

    private void onContinueClicked() {
        final int dogWalkerId = R.id.dog_walker;
        final int dogOwnerId = R.id.dog_owner;
        final int radioButtonId = radioGroup.getCheckedRadioButtonId();
        if (radioButtonId == dogWalkerId) listener.setFindDogWalkers(true);
        else if (radioButtonId == dogOwnerId) listener.setFindDogWalkers(false);
        dismiss();
    }
}
