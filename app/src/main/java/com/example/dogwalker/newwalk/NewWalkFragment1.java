package com.example.dogwalker.newwalk;

import com.example.dogwalker.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class NewWalkFragment1 extends DialogFragment {

    private NewWalkFragmentTracker listener;
    private boolean findDogWalkers;

    private TextView textView;
    private RadioGroup radioGroup;
    private RadioButton radioButton;

    public static NewWalkFragment1 newInstance(int layout, boolean findDogWalkers) {
        NewWalkFragment1 fragment = new NewWalkFragment1();
        Bundle args= new Bundle();
        args.putInt("layout", layout);
        args.putBoolean("find_dog_walkers", findDogWalkers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (findDogWalkers) {
            textView.setText(R.string.new_walk_fragment_1_title_walker);
            radioButton.setText(R.string.from_walkers_search);
        } else {
            textView.setText(R.string.new_walk_fragment_1_title_owner);
            radioButton.setText(R.string.from_owners_search);
        }
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
        findDogWalkers = args.getBoolean("find_dog_walkers");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layout, null);

        textView = view.findViewById(R.id.fragment_title);
        radioGroup = view.findViewById(R.id.radio_group);
        radioButton = view.findViewById(R.id.from_nearby);

        return builder.setView(view).setTitle(R.string.start_walk).setIcon(R.drawable.dog_walker)
                .setPositiveButton("Continue", (dialog, which) -> onContinueClicked())
                .setNegativeButton("Cancel", (dialog, which) -> dismiss()).create();
    }

    private void onContinueClicked() {
        final int fromContactsId = R.id.from_contacts;
        final int fromNearbyId = R.id.from_nearby;
        final int radioButtonId = radioGroup.getCheckedRadioButtonId();
        if (radioButtonId == fromContactsId) listener.setFromContacts(true);
        else if (radioButtonId == fromNearbyId) listener.setFromContacts(false);
        dismiss();
    }
}
