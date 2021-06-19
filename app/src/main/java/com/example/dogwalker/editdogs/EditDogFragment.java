package com.example.dogwalker.editdogs;

import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.Dog;
import com.example.dogwalker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
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
    private long birthDate = -1;
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

        view.findViewById(R.id.add_profile_picture).setOnClickListener(v -> listener.onAddDogPictureButtonClick(v));
        view.findViewById(R.id.add_health_and_needs).setOnClickListener(this::addNewHealthAndNeeds);
        view.findViewById(R.id.add_walker_requirements).setOnClickListener(this::addNewWalkerRequirements);

        birthDateView.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(getContext(), EditDogFragment.this,
                    calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        view.findViewById(R.id.save_dog).setOnClickListener(v -> {
            if (dogName.getText().toString().equals(""))
                Toast.makeText(getContext(), "Your dog must have a name!", Toast.LENGTH_SHORT).show();
            else dismiss();
        });

        if (getTag().equals("edit_dog")) getDogFromDatabase();
        else dog = new Dog();
    }

    private void getDogFromDatabase() {
        //TODO: use dogId to get the dog from DB & set values
    }

    public void setProfilePicturePreview(Uri imageUri) {
        profilePictureUri = imageUri;
        profilePicture.setBackground(null);
        Picasso.get().load(profilePictureUri.toString()).transform(new CircleTransform()).into(profilePicture);
    }

    private void addNewHealthAndNeeds(View v) {

        int insertIndex = infoAndHealthNeedsLayout.indexOfChild(view.findViewById(((RelativeLayout) v.getParent()).getId())) + 1;

        Button button = new Button(getContext());
        button.setId(View.generateViewId());
        button.setBackgroundResource(R.drawable.ic_add);
        button.setGravity(Gravity.TOP);
        button.setWidth(30);
        button.setHeight(30);
        button.setOnClickListener(this::addNewHealthAndNeeds);

        EditText editText = new EditText(getContext());
        RelativeLayout.LayoutParams paramsET = new RelativeLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsET.addRule(RelativeLayout.LEFT_OF, button.getId());
        editText.setLayoutParams(paramsET);
        editText.setId(View.generateViewId());
        editText.setBackgroundResource(R.drawable.text_input_layout_style);

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setId(View.generateViewId());
        relativeLayout.addView(editText);
        relativeLayout.addView(button);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END, button.getId());
        params.addRule(RelativeLayout.ALIGN_PARENT_START, editText.getId());
        relativeLayout.setLayoutParams(params);

        infoAndHealthNeedsLayout.addView(relativeLayout, insertIndex);
        dogInfoAndHealthNeeds.add(insertIndex, editText);
    }

    private void addNewWalkerRequirements(View v) {

        int insertIndex = walkerRequirementsLayout.indexOfChild(view.findViewById(((RelativeLayout) v.getParent()).getId())) + 1;

        Button button = new Button(getContext());
        button.setId(View.generateViewId());
        button.setBackgroundResource(R.drawable.ic_add);
        button.setGravity(Gravity.TOP);
        button.setWidth(30);
        button.setHeight(30);
        button.setOnClickListener(this::addNewWalkerRequirements);

        EditText editText = new EditText(getContext());
        RelativeLayout.LayoutParams paramsET = new RelativeLayout.LayoutParams(0, RelativeLayout.LayoutParams.WRAP_CONTENT);
        paramsET.addRule(RelativeLayout.LEFT_OF, button.getId());
        editText.setLayoutParams(paramsET);
        editText.setId(View.generateViewId());
        editText.setBackgroundResource(R.drawable.text_input_layout_style);

        RelativeLayout relativeLayout = new RelativeLayout(getContext());
        relativeLayout.setId(View.generateViewId());
        relativeLayout.addView(editText);
        relativeLayout.addView(button);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_END, button.getId());
        params.addRule(RelativeLayout.ALIGN_PARENT_START, editText.getId());
        relativeLayout.setLayoutParams(params);

        walkerRequirementsLayout.addView(relativeLayout, insertIndex);
        dogWalkerRequirements.add(insertIndex, editText);
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

    @Override
    public void onDetach() {
        super.onDetach();

        String breed = dogBreed.getText().toString();
        boolean newProfilePicture = false;
        String aboutMe = aboutDog.getText().toString();
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

        if (!breed.equals("") || dog.getBreed() != null || !dog.getBreed().equals("")) dog.setBreed(breed);
        if (!aboutMe.equals("") || dog.getProfileAboutMe() != null || dog.getProfileAboutMe().equals("")) dog.setProfileAboutMe(aboutMe);
        if (birthDate == -1 && dog.getBirthDate() != null) dog.setBirthDate(null);
        if (birthDate != -1) dog.setBirthDate(birthDate);

        if (profilePictureUri != null) {
            dog.setProfilePicture(profilePictureUri.toString());
            newProfilePicture = true;
        }

        if (!averageWalkLength.equals("")) {
            dog.setAverageWalkLength(Integer.parseInt(averageWalkLength));
            dog.setWalkLengthUnits(walkLengthUnits.getSelectedItem().toString());
        } else if (dog.getAverageWalkLength() == null) {
            dog.setAverageWalkLength(null);
            dog.setWalkLengthUnits(null);
        }

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

        if (getTag().equals("add_dog")) listener.setDog(dog, dogId, true, newProfilePicture);
        else listener.setDog(dog, dogId, false, newProfilePicture);
    }
}
