package com.example.dogwalker;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Objects;

public class AuthActionFragment extends DialogFragment {

    AuthActionDialogListener listener;

    public static AuthActionFragment newInstance(int layout, String action, String email) {
        AuthActionFragment fragment = new AuthActionFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putString("action", action);
        args.putString("email", email);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = Objects.requireNonNull(getDialog()).getWindow().getAttributes();
        params.width = ActionBar.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AuthActionDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AuthActionDialogListener interface.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int layout = args.getInt("layout");
        String action = args.getString("action");
        String email = args.getString("email");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layout, null);

        final EditText emailAddress = view.findViewById(R.id.email_address);
        final Button sendEmail = view.findViewById(R.id.send_email);
        if (!email.equals("")) emailAddress.setText(email);

        if (action.equals("verify_email")) {
            final EditText password = view.findViewById(R.id.password);
            sendEmail.setOnClickListener(v ->
                    listener.onVerifyEmailAttempt(this, emailAddress.getText().toString(), password.getText().toString()));
        } else if (action.equals("reset_password")) {
            sendEmail.setOnClickListener(v ->
                    listener.onResetPasswordAttempt(this, emailAddress.getText().toString()));
        }

        return builder.setView(view).create();
    }
}