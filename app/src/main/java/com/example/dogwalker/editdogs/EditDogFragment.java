package com.example.dogwalker.editdogs;

import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.Dog;
import com.example.dogwalker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EditDogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;

    private EditDogDialogListener listener;
    private View view;
    private Dog dog;
    private String dogId;
    private int height = 0;

    private ImageView profilePicture;
    private EditText dogName;
    private EditText dogBreed;
    private EditText birthDateView;
    private EditText aboutDog;
    private EditText walkLength;
    private Spinner walkLengthUnits;
    private RadioGroup trainingLevel;
    private LinearLayout infoAndHealthNeedsLayout;
    private LinearLayout walkerRequirementsLayout;

    private Uri profilePictureUri = null;
    private Long birthDate = null;
    private List<EditText> dogInfoAndHealthNeeds = new ArrayList<>();
    private List<EditText> dogWalkerRequirements = new ArrayList<>();

    public static EditDogFragment newInstance(int layout, String dogId, int height) {
        EditDogFragment fragment = new EditDogFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putString("dog_id", dogId);
        args.putInt("height", height);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        WindowManager.LayoutParams params = Objects.requireNonNull(getDialog()).getWindow().getAttributes();
        params.height = height;
        getDialog().getWindow().setAttributes(params);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (EditDogDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement AddDogPictureListener.");
        }
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle args = getArguments();
        int layout = args.getInt("layout");
        dogId = args.getString("dog_id");
        height = args.getInt("height");

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        view = inflater.inflate(layout, null);
        setDialogFragmentUI();

        return builder.setView(view).create();
    }

    private void setDialogFragmentUI() {

        profilePicture = view.findViewById(R.id.profile_picture);
        dogName = view.findViewById(R.id.dog_name);
        dogBreed = view.findViewById(R.id.dog_breed);
        birthDateView = view.findViewById(R.id.birth_date);
        aboutDog = view.findViewById(R.id.about);
        walkLength = view.findViewById(R.id.walk_length);
        walkLengthUnits = view.findViewById(R.id.walk_length_units);
        trainingLevel = view.findViewById(R.id.training_level);
        infoAndHealthNeedsLayout = view.findViewById(R.id.health_and_needs);
        walkerRequirementsLayout = view.findViewById(R.id.walker_requirements);

        birthDateView.setOnClickListener(v -> setBirthDate(true));
        birthDateView.setOnFocusChangeListener((v, hasFocus) -> setBirthDate(hasFocus));
        view.findViewById(R.id.add_profile_picture).setOnClickListener(v -> listener.onAddDogPictureButtonClick(v));
        view.findViewById(R.id.add_health_and_needs).setOnClickListener(v -> addNewHealthAndNeeds());
        view.findViewById(R.id.add_walker_requirements).setOnClickListener(v -> addNewWalkerRequirements());
        view.findViewById(R.id.save_dog).setOnClickListener(v -> saveButton());

        if (getTag().equals("edit_dog")) getDogFromDatabase();
        else dog = new Dog();
    }

    private void getDogFromDatabase() {
        //TODO: use dogId to get the dog from DB & set values
    }

    private void setBirthDate(boolean hasFocus) {
        if (hasFocus) {
            final Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(), EditDogFragment.this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String dateDisplay = month + "/" + dayOfMonth + "/" + year;
        birthDateView.setText(dateDisplay);
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        birthDate = calendar.getTimeInMillis();
    }

    public void setProfilePicturePreview(Uri imageUri) {
        profilePictureUri = imageUri;
        profilePicture.setBackground(null);
        Picasso.get().load(profilePictureUri.toString()).transform(new CircleTransform()).into(profilePicture);
    }

    private void addNewHealthAndNeeds() {
        EditText editText = new EditText(getContext());
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        infoAndHealthNeedsLayout.addView(editText);
        dogInfoAndHealthNeeds.add(editText);
    }

    private void addNewWalkerRequirements() {
        EditText editText = new EditText(getContext());
        editText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        walkerRequirementsLayout.addView(editText);
        dogWalkerRequirements.add(editText);
    }

    private void saveButton() {
        if (dogName.getText().toString().equals(""))
            Toast.makeText(getContext(), "Your dog must have a name!", Toast.LENGTH_SHORT).show();
        else saveDog();
    }

    private void saveDog() {

        boolean newProfilePicture = false;
        String breed = dogBreed.getText().toString();
        String about = aboutDog.getText().toString();
        String averageWalkLength = walkLength.getText().toString();
        final int untrainedId = R.id.untrained;
        final int minimallyTrainedId = R.id.minimally_trained;
        final int wellTrainedId = R.id.well_trained;
        final int expertTrainingId = R.id.expert_training;
        final int unknownTrainingId = R.id.unknown_training;
        Map<Integer, String> infoAndHealthNeeds = dog.getInfoAndHealthNeeds();
        Map<Integer, String> walkerRequirements = dog.getWalkerRequirements();
        int index;

        dog.setName(dogName.getText().toString());
        dog.setOwner(currentUser.getUid());

        if (breed != null && !breed.equals("")) dog.setBreed(breed);
        if (about != null && !about.equals("")) dog.setProfileAboutMe(about);

        if (birthDate != null) {
            if (birthDate <= Calendar.getInstance().getTimeInMillis()) dog.setBirthDate(birthDate);
            else Toast.makeText(getContext(), "Invalid birth date!", Toast.LENGTH_SHORT).show();
        }

        if (profilePictureUri != null) {
            dog.setProfilePicture(profilePictureUri.toString());
            newProfilePicture = true;
        }

        if (averageWalkLength == null || averageWalkLength.equals("")) dog.setAverageWalkLength(null);
        else dog.setAverageWalkLength(averageWalkLength + " " + walkLengthUnits.getSelectedItem().toString());

        switch (trainingLevel.getCheckedRadioButtonId()) {
            case untrainedId:
                dog.setTrainingLevel(getString(R.string.untrained));
                break;
            case minimallyTrainedId:
                dog.setTrainingLevel(getString(R.string.minimally_trained));
                break;
            case wellTrainedId:
                dog.setTrainingLevel(getString(R.string.well_trained));
                break;
            case expertTrainingId:
                dog.setTrainingLevel(getString(R.string.expert_training));
                break;
            case unknownTrainingId:
                dog.setTrainingLevel(getString(R.string.unknown_training));
                break;
        }

        index = 0;
        infoAndHealthNeeds.clear();
        for (int i = 0; i < dogInfoAndHealthNeeds.size(); i++) {
            String entry = dogInfoAndHealthNeeds.get(i).getText().toString();
            if (!entry.equals("")) {
                infoAndHealthNeeds.put(index, entry);
                index++;
            }
        }
        dog.setInfoAndHealthNeeds(infoAndHealthNeeds);

        index = 0;
        walkerRequirements.clear();
        for (int i = 0; i < dogWalkerRequirements.size(); i++) {
            String entry = dogWalkerRequirements.get(i).getText().toString();
            if (!entry.equals("")) {
                walkerRequirements.put(index, entry);
                index++;
            }
        }
        dog.setWalkerRequirements(walkerRequirements);

        listener.setDog(dog, dogId, getTag().equals("add_dog"), newProfilePicture);
        dismiss();
    }
}
