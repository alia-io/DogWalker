package com.example.dogwalker.setupprofile;

import com.example.dogwalker.R;
import com.example.dogwalker.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SetUpProfileActivity extends AppCompatActivity implements FragmentTracker {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private User user;

    private Fragment fragment1;
    private Fragment fragment2;
    private int currentFragment = 0;

    private ImageView leftArrow;
    private ImageView rightArrow;
    private GestureDetectorCompat gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("fragment", "beginning of onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users/" + currentUser.getUid());

        leftArrow = findViewById(R.id.left_arrow);
        rightArrow = findViewById(R.id.right_arrow);

        leftArrow.setOnClickListener(v -> goPrevious());
        rightArrow.setOnClickListener(v -> goNext());
        gestureDetector = new GestureDetectorCompat(this, new GestureListener());

        fragment1 = new Fragment1();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        goNext();
        Log.d("fragment", "end of onCreate");
    }

    @Override
    public void goNext() {
        if (currentFragment == 0) {
            loadFragment(fragment1);
            rightArrow.setVisibility(View.VISIBLE);
        } else if (currentFragment == 1) {
            loadFragment(fragment2);
            leftArrow.setVisibility(View.VISIBLE);
        } else return;
        currentFragment++;
    }

    @Override
    public void goPrevious() {
        if (currentFragment == 2) {
            loadFragment(fragment1);
            leftArrow.setVisibility(View.INVISIBLE);
        } else return;
        currentFragment--;
    }

    @Override
    public void saveFragment1(boolean dogOwner, boolean dogWalker, int ownerY, int ownerM, int ownerD, int walkerY, int walkerM, int walkerD) {
        user.setDogOwner(dogOwner);
        user.setDogWalker(dogWalker);
        Calendar currentCalendar = new GregorianCalendar();
        if (dogOwner) {
            Calendar ownerCalendar = new GregorianCalendar();
            ownerCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR) - ownerY);
            ownerCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH) - ownerM);
            ownerCalendar.set(Calendar.DATE, currentCalendar.get(Calendar.DATE) - ownerD);
            user.setDogOwnerExperience(new Timestamp(ownerCalendar.getTimeInMillis()));
        }
        if (dogWalker) {
            Calendar walkerCalendar = new GregorianCalendar();
            walkerCalendar.set(Calendar.YEAR, currentCalendar.get(Calendar.YEAR) - walkerY);
            walkerCalendar.set(Calendar.MONTH, currentCalendar.get(Calendar.MONTH) - walkerM);
            walkerCalendar.set(Calendar.DATE, currentCalendar.get(Calendar.DATE) - walkerD);
            user.setDogWalkerExperience(new Timestamp(walkerCalendar.getTimeInMillis()));
        }
    }

    @Override
    public void finished() {
        if (!user.isDogOwner() && !user.isDogWalker()) {
            // TODO: need to be either dog owner or dog walker
            return;
        }
        // TODO: save to DB and go to next activity
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event1.getX() < event2.getX()) {
                goPrevious();
                return true;
            } else if (event1.getX() > event2.getX()) {
                goNext();
                return true;
            }
            return false;
        }
    }
}