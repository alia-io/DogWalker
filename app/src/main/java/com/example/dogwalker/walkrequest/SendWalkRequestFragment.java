package com.example.dogwalker.walkrequest;

import com.example.dogwalker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SendWalkRequestFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private SendWalkRequestTracker listener;

    private LinearLayout dogLayout;
    private RadioGroup dateTime;
    private RadioButton setTimeAsap;
    private RadioButton setDateTime;
    private TextView dateTimeText;
    private EditText compensation1;
    private EditText compensation2;
    private Spinner currencySpinner;
    private EditText messageText;

    private String targetUserId;
    private String targetUserName;
    private boolean targetIsWalker;
    private Map<String, String> allDogsByName;

    private long walkTime;
    private int year;
    private int month;
    private int dayOfMonth;

    public static SendWalkRequestFragment newInstance(int layout, boolean targetIsWalker, String targetUserId, String targetUserName) {
        SendWalkRequestFragment fragment = new SendWalkRequestFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putBoolean("target_walker", targetIsWalker);
        args.putString("target_id", targetUserId);
        args.putString("target_name", targetUserName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ownerDogsRef;
        if (targetIsWalker)
            ownerDogsRef = database.getReference("Users/" + targetUserId + "/dogs");
        else
            ownerDogsRef = FirebaseDatabase.getInstance().getReference("Users/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/dogs");
        ownerDogsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null && snapshot.hasChildren() && snapshot.getChildrenCount() > 0) {
                    Iterable<DataSnapshot> dogSnapshots = snapshot.getChildren();
                    for (DataSnapshot dogSnapshot : dogSnapshots) {
                        if (dogSnapshot != null && dogSnapshot.getKey() != null && dogSnapshot.getValue() != null
                                && Boolean.parseBoolean(dogSnapshot.getValue().toString())) {
                            String dogId = dogSnapshot.getKey();
                            database.getReference("Dogs/" + dogId + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                    if (snapshot1 != null && snapshot1.getValue() != null) {
                                        String dogName = snapshot1.getValue().toString();
                                        allDogsByName.put(dogName, dogId);
                                        CheckBox checkBox = new CheckBox(getContext());
                                        checkBox.setText(dogName);
                                        dogLayout.addView(checkBox);
                                    }
                                }
                                @Override public void onCancelled(@NonNull DatabaseError error) { }
                            });
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (SendWalkRequestTracker) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement SendWalkRequestTracker interface.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        int layout = args.getInt("layout");
        targetIsWalker = args.getBoolean("target_walker");
        targetUserId = args.getString("target_id");
        targetUserName = args.getString("target_name");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(layout, null);

        allDogsByName = new HashMap<>();
        dogLayout = view.findViewById(R.id.dogs);
        dateTime = view.findViewById(R.id.walk_time);
        setTimeAsap = view.findViewById(R.id.asap);
        setDateTime = view.findViewById(R.id.set_date_time);
        dateTimeText = view.findViewById(R.id.date_time_text);
        compensation1 = view.findViewById(R.id.compensation_1);
        compensation2 = view.findViewById(R.id.compensation_2);
        currencySpinner = view.findViewById(R.id.currency);
        messageText = view.findViewById(R.id.message);

        setTimeAsap.setOnCheckedChangeListener((buttonView, isChecked) -> onAsapClicked(isChecked));
        setDateTime.setOnCheckedChangeListener((buttonView, isChecked) -> onSetDateTimeClicked(isChecked));

        return builder.setView(view)
                .setTitle(getActivity().getResources().getString(R.string.request_walk) + " " + targetUserName)
                .setIcon(R.drawable.dog_walker)
                .setPositiveButton("Send Request", (dialog, which) -> onSendRequest())
                .setNegativeButton("Cancel", (dialog, which) -> dismiss())
                .create();
    }

    private void onAsapClicked(boolean isChecked) {
        if (isChecked) dateTimeText.setVisibility(View.GONE);
    }

    private void onSetDateTimeClicked(boolean isChecked) {
        if (isChecked) {
            final Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(), SendWalkRequestFragment.this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.year = year;
        this.month = month;
        this.dayOfMonth = dayOfMonth;
        final Calendar calendar = Calendar.getInstance();
        new TimePickerDialog(getContext(), SendWalkRequestFragment.this,
                calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        walkTime = calendar.getTimeInMillis();
        String displayDateTime = month + "/" + dayOfMonth + "/" + year + " " + hourOfDay + ":" + minute;
        dateTimeText.setText(displayDateTime);
        dateTimeText.setVisibility(View.VISIBLE);
    }

    private void onSendRequest() {

        Map<String, String> selectedDogsById = new HashMap<>();
        for (int i = 0; i < dogLayout.getChildCount(); i++) {
            CheckBox dog = (CheckBox) dogLayout.getChildAt(i);
            if (dog.isChecked()) {
                String dogName = dog.getText().toString();
                if (dogName != null) {
                    String dogId = allDogsByName.get(dogName);
                    if (dogId != null)
                        selectedDogsById.put(dogId, dogName);
                }
            }
        }
        if (selectedDogsById.isEmpty()) {
            Toast.makeText(getContext(), "You must select at least one dog to be walked", Toast.LENGTH_SHORT).show();
            return;
        }

        final int asapId = R.id.asap;
        final int setDateTimeId = R.id.set_date_time;
        switch (dateTime.getCheckedRadioButtonId()) {
            case asapId:
                walkTime = 0;
                break;
            case setDateTimeId:
                if (walkTime <= Calendar.getInstance().getTimeInMillis()) {
                    Toast.makeText(getContext(), "The selected time has already passed!", Toast.LENGTH_SHORT).show();
                    return;
                }
            default:
                Toast.makeText(getContext(), "Please select a time", Toast.LENGTH_SHORT).show();
                return;
        }

        String dollarString = compensation1.getText().toString();
        String centString = compensation2.getText().toString();
        int dollars = 0;
        int cents = 0;
        if (dollarString != null && !dollarString.equals("")) dollars = Math.abs(Integer.parseInt(dollarString));
        if (centString != null && !centString.equals("")) cents = Math.abs(Integer.parseInt(centString));
        float total = dollars;
        if (cents < 10) total += (float) (cents / 10);
        else total += (float) (cents / 100);
        if (total <= 0) {
            Toast.makeText(getContext(), "Please enter a proposed compensation amount.", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedCurrencySymbol = "";
        String selectedCurrencyName = currencySpinner.getSelectedItem().toString();
        String[] currencyNames = getContext().getResources().getStringArray(R.array.currencies);
        String[] currencySymbols = getContext().getResources().getStringArray(R.array.currency_symbols);
        for (int i = 0; i < currencyNames.length && i < currencySymbols.length; i++) {
            if (currencyNames[i].equals(selectedCurrencyName)) {
                selectedCurrencySymbol = currencySymbols[i];
            }
        }

        String message = messageText.getText().toString();
        if (message == null) message = "";

        listener.setWalkRequest(targetUserId, targetUserName, targetIsWalker, selectedDogsById, walkTime, total, selectedCurrencySymbol, message);
    }
}
