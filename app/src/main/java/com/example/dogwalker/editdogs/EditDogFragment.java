package com.example.dogwalker.editdogs;

import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.Dog;
import com.example.dogwalker.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class EditDogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private EditDogDialogListener listener;
    private View view;
    private Dog dog;
    private String dogKey;
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
    private final List<EditText> dogInfoAndHealthNeeds = new ArrayList<>();
    private final List<EditText> dogWalkerRequirements = new ArrayList<>();

    public static EditDogFragment newInstance(int layout, String dogKey, int height) {
        EditDogFragment fragment = new EditDogFragment();
        Bundle args = new Bundle();
        args.putInt("layout", layout);
        args.putString("dog_key", dogKey);
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
        dogKey = args.getString("dog_key");
        height = args.getInt("height");

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

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
        view.findViewById(R.id.add_profile_picture).setOnClickListener(this::displayProfilePicturePopupMenu);
        view.findViewById(R.id.add_health_and_needs).setOnClickListener(v -> addNewHealthAndNeeds());
        view.findViewById(R.id.add_walker_requirements).setOnClickListener(v -> addNewWalkerRequirements());
        view.findViewById(R.id.save_dog).setOnClickListener(v -> saveButton());

        if (getTag().equals("edit_dog")) getDogFromDatabase();
        else dog = new Dog();
    }

    private void getDogFromDatabase() {
        database.getReference("Dogs/" + dogKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                dog = snapshot.getValue(Dog.class);
                if (dog != null) {

                    dogName.setText(dog.getName());
                    if (dog.getBreed() != null) dogBreed.setText(dog.getBreed());
                    if (dog.getProfileAboutMe() != null) aboutDog.setText(dog.getProfileAboutMe());

                    if (dog.getProfilePicture() != null)
                        Picasso.get().load(dog.getProfilePicture()).transform(new CircleTransform()).into(profilePicture);

                    if (dog.getBirthDate() != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(Long.parseLong(dog.getBirthDate().toString()));
                        String dateDisplay = calendar.get(Calendar.MONTH) + "/" + calendar.get(Calendar.DAY_OF_MONTH)
                                + "/" + calendar.get(Calendar.YEAR);
                        birthDateView.setText(dateDisplay);
                    }

                    if (dog.getTrainingLevel() != null) {
                        String training = dog.getTrainingLevel();
                        if (getString(R.string.untrained).equals(training)) {
                            ((RadioButton) view.findViewById(R.id.untrained)).setChecked(true);
                        } else if (getString(R.string.minimally_trained).equals(training)) {
                            ((RadioButton) view.findViewById(R.id.minimally_trained)).setChecked(true);
                        } else if (getString(R.string.well_trained).equals(training)) {
                            ((RadioButton) view.findViewById(R.id.well_trained)).setChecked(true);
                        } else if (getString(R.string.expert_training).equals(training)) {
                            ((RadioButton) view.findViewById(R.id.expert_training)).setChecked(true);
                        } else if (getString(R.string.unknown_training).equals(training)) {
                            ((RadioButton) view.findViewById(R.id.unknown_training)).setChecked(true);
                        }
                    }

                    if (dog.getAverageWalkLength() != null) {
                        String[] walkDistance = dog.getAverageWalkLength().split(" ");
                        if (walkDistance.length == 2) {
                            walkLength.setText(walkDistance[0]);
                            String[] walkUnits = getResources().getStringArray(R.array.dog_walk_length_units);
                            for (int i = 0; i < walkUnits.length; i++) {
                                if (walkUnits[i].equals(walkDistance[1])) {
                                    walkLengthUnits.setSelection(i);
                                    break;
                                }
                            }
                        }
                    }

                    if (dog.getInfoAndHealthNeeds() != null && dog.getInfoAndHealthNeeds().size() > 0) {
                        List<String> needs = dog.getInfoAndHealthNeeds();
                        for (int i = 0; i < needs.size(); i++) {
                            addNewHealthAndNeeds();
                            dogInfoAndHealthNeeds.get(i).setText(needs.get(i));
                        }
                    }

                    if (dog.getWalkerRequirements() != null && dog.getWalkerRequirements().size() > 0) {
                        List<String> requirements = dog.getWalkerRequirements();
                        for (int i = 0; i < requirements.size(); i++) {
                            addNewWalkerRequirements();
                            dogWalkerRequirements.get(i).setText(requirements.get(i));
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void displayProfilePicturePopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_picture_popup, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            final int takePicture = R.id.action_take_picture;
            final int uploadPicture = R.id.action_upload_picture;
            switch (item.getItemId()) {
                case takePicture:
                    listener.takeProfilePicture();
                    return true;
                case uploadPicture:
                    listener.uploadProfilePicture();
                    return true;
                default: return false;
            }
        });
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
        if (birthDate > Calendar.getInstance().getTimeInMillis()) {
            Toast.makeText(getContext(), "Invalid birth date!", Toast.LENGTH_SHORT).show();
            birthDate = null;
        }
    }

    public void setProfilePicturePreview(Uri imageUri) {
        profilePictureUri = imageUri;
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
        List<String> infoAndHealthNeeds = dog.getInfoAndHealthNeeds();
        List<String> walkerRequirements = dog.getWalkerRequirements();

        dog.setName(dogName.getText().toString());
        dog.setOwner(currentUser.getUid());

        if (breed != null) dog.setBreed(breed);
        if (about != null) dog.setProfileAboutMe(about);
        if (birthDate != null) dog.setBirthDate(birthDate);

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

        infoAndHealthNeeds.clear();
        for (int i = 0; i < dogInfoAndHealthNeeds.size(); i++) {
            String entry = dogInfoAndHealthNeeds.get(i).getText().toString();
            if (entry != null && !entry.equals(""))
                infoAndHealthNeeds.add(entry);
        }
        dog.setInfoAndHealthNeeds(infoAndHealthNeeds);

        walkerRequirements.clear();
        for (int i = 0; i < dogWalkerRequirements.size(); i++) {
            String entry = dogWalkerRequirements.get(i).getText().toString();
            if (entry != null && !entry.equals(""))
                walkerRequirements.add(entry);
        }
        dog.setWalkerRequirements(walkerRequirements);

        listener.setDog(dogKey, dog, getTag().equals("add_dog"), newProfilePicture);
        dismiss();
    }
}
