package com.example.dogwalker.walkrequest;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.dogwalker.R;

public class SelectWalkRoleFragment extends DialogFragment {

    private SendWalkRequestTracker listener;
    private Resources resources;
    private String targetUserId;
    private String targetUserName;

    private TextView titleText;
    private RadioGroup roleGroup;
    private RadioButton ownerButton;
    private RadioButton walkerButton;

    public static SelectWalkRoleFragment newInstance(int layout, String targetUserId, String targetUserName) {
        SelectWalkRoleFragment fragment = new SelectWalkRoleFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putString("target_id", targetUserId);
        args.putString("target_name", targetUserName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        String title = resources.getString(R.string.select_walk_role_title_1) + " " + targetUserName + " " + resources.getString(R.string.select_walk_role_title_2);
        String option1 = targetUserName + " " + resources.getString(R.string.select_walk_role_option_1);
        String option2 = resources.getString(R.string.select_walk_role_option_2_a) + " " + targetUserName + " " + resources.getString(R.string.select_walk_role_option_2_b);
        titleText.setText(title);
        ownerButton.setText(option1);
        walkerButton.setText(option2);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (SendWalkRequestTracker) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SendWalkRequestTracker interface.");
        }
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int layout = args.getInt("layout");
        targetUserId = args.getString("target_id");
        targetUserName = args.getString("target_name");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layout, null);

        resources = getActivity().getResources();
        titleText = view.findViewById(R.id.title);
        roleGroup = view.findViewById(R.id.role);
        ownerButton = view.findViewById(R.id.owner);
        walkerButton = view.findViewById(R.id.walker);

        return builder.setView(view)
                .setTitle(getActivity().getResources().getString(R.string.request_walk_for) + " " + targetUserName)
                .setIcon(R.drawable.dog_walker)
                .setPositiveButton("Next", (dialog, which) -> onNextClicked())
                .setNegativeButton("Cancel", (dialog, which) -> dismiss())
                .create();
    }

    private void onNextClicked() {
        final int ownerId = R.id.owner;
        final int walkerId = R.id.walker;
        switch (roleGroup.getCheckedRadioButtonId()) {
            case ownerId:
                listener.setIsTargetWalker(targetUserId, targetUserName, true);
                break;
            case walkerId:
                listener.setIsTargetWalker(targetUserId, targetUserName, false);
                break;
        }
        dismiss();
    }
}
