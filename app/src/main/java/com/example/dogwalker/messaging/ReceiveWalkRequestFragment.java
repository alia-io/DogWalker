package com.example.dogwalker.messaging;

import com.example.dogwalker.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ReceiveWalkRequestFragment extends DialogFragment {

    private ReceiveWalkRequestListener listener;
    private TextView dogWalkerView;
    private TextView dogsView;
    private TextView whenView;
    private TextView paymentView;
    private TextView messageTitle;
    private TextView messageView;
    private String walkerName;
    private String dogs;
    private String when;
    private String payment;
    private String message;

    public static ReceiveWalkRequestFragment newInstance(int layout, String notificationKey, String senderName, String walkerName,
                                                         String dogs, String when, String payment, String message) {
        ReceiveWalkRequestFragment fragment = new ReceiveWalkRequestFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putString("notification_key", notificationKey);
        args.putString("sender_name", senderName);
        args.putString("walker_name", walkerName);
        args.putString("dogs", dogs);
        args.putString("when", when);
        args.putString("payment", payment);
        args.putString("message", message);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        dogWalkerView.setText(walkerName);
        dogsView.setText(dogs);
        whenView.setText(when);
        paymentView.setText(payment);
        if (message != null && !message.equals("")) {
            messageView.setText(message);
            messageView.setVisibility(View.VISIBLE);
            messageTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ReceiveWalkRequestListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ReceiveWalkRequestListener interface.");
        }
    }

    @Override @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int layout = args.getInt("layout");
        String notificationKey = args.getString("notification_key");
        String senderName = args.getString("sender_name");
        walkerName = args.getString("walker_name");
        dogs = args.getString("dogs");
        when = args.getString("when");
        payment = args.getString("payment");
        message = args.getString("message");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layout, null);

        dogWalkerView = view.findViewById(R.id.dog_walker);
        dogsView = view.findViewById(R.id.dogs_list);
        whenView = view.findViewById(R.id.date_time);
        paymentView = view.findViewById(R.id.payment);
        messageTitle = view.findViewById(R.id.message_title);
        messageView = view.findViewById(R.id.message);

        return builder.setView(view)
                .setTitle(getActivity().getResources().getString(R.string.request_walk_from) + " " + senderName)
                .setIcon(R.drawable.dog_walker)
                .setPositiveButton("Accept Request", (dialog, which) -> listener.acceptWalkRequest(notificationKey))
                .setNegativeButton("Decline Request", (dialog, which) -> listener.declineWalkRequest(notificationKey))
                .setNeutralButton("Close", (dialog, which) -> dismiss())
                .create();
    }
}
