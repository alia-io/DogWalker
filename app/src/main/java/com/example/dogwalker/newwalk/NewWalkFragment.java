package com.example.dogwalker.newwalk;

import com.example.dogwalker.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class NewWalkFragment extends DialogFragment {

    private NewWalkFragmentTracker listener;
    private Fragment currentFragment;
    private boolean nextFragment;
    private boolean findDogWalkers;

    public static NewWalkFragment newInstance(int layout, boolean nextFragment, boolean findDogWalkers) {
        NewWalkFragment fragment = new NewWalkFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putBoolean("next_fragment", nextFragment);
        args.putBoolean("find_dog_walkers", findDogWalkers);
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
        nextFragment = args.getBoolean("next_fragment");
        findDogWalkers = args.getBoolean("find_dog_walkers");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layout, null);

        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (nextFragment) currentFragment = new FirstFragment();
        else {
            currentFragment = new LastFragment();
            if (findDogWalkers) {
                ((TextView) currentFragment.getView().findViewById(R.id.fragment_title)).setText(R.string.new_walk_fragment_1_title_walker);
                ((RadioButton) currentFragment.getView().findViewById(R.id.from_nearby)).setText(R.string.from_walkers_search);
            }
            else {
                ((TextView) currentFragment.getView().findViewById(R.id.fragment_title)).setText(R.string.new_walk_fragment_1_title_owner);
                ((RadioButton) currentFragment.getView().findViewById(R.id.from_nearby)).setText(R.string.from_owners_search);
            }
        }

        fragmentTransaction.replace(R.id.fragment_container, currentFragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();

        return builder.setView(view).setPositiveButton("Continue", (dialog, which) -> onContinueClicked())
                .setNegativeButton("Cancel", (dialog, which) -> dismiss()).create();
    }

    private void onContinueClicked() {

        RadioGroup radioGroup = currentFragment.getView().findViewById(R.id.radio_group);

        if (nextFragment) {

            currentFragment = new LastFragment();
            final int dogWalkerId = R.id.dog_walker;
            final int dogOwnerId = R.id.dog_owner;
            final int radioButtonId = radioGroup.getCheckedRadioButtonId();

            if (radioButtonId == dogWalkerId) {
                listener.setFindDogWalkers(true);
                ((TextView) currentFragment.getView().findViewById(R.id.fragment_title)).setText(R.string.new_walk_fragment_1_title_walker);
                ((RadioButton) currentFragment.getView().findViewById(R.id.from_nearby)).setText(R.string.from_walkers_search);
            } else if (radioButtonId == dogOwnerId) {
                listener.setFindDogWalkers(false);
                ((TextView) currentFragment.getView().findViewById(R.id.fragment_title)).setText(R.string.new_walk_fragment_1_title_owner);
                ((RadioButton) currentFragment.getView().findViewById(R.id.from_nearby)).setText(R.string.from_owners_search);
            }

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, currentFragment);
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            fragmentTransaction.commit();
            nextFragment = false;

        } else {

            final int fromContactsId = R.id.from_contacts;
            final int fromNearbyId = R.id.from_nearby;
            final int radioButtonId = radioGroup.getCheckedRadioButtonId();

            if (radioButtonId == fromContactsId) listener.setFromContacts(true);
            else if (radioButtonId == fromNearbyId) listener.setFromContacts(false);

            dismiss();
        }
    }

    public static class FirstFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_new_walk_0, container, false);
        }
    }

    public static class LastFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_new_walk_1, container, false);
        }
    }
}
