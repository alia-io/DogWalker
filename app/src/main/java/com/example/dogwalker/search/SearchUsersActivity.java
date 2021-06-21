package com.example.dogwalker.search;

import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogwalker.LocationUpdatingAppCompatActivity;
import com.example.dogwalker.R;

public class SearchUsersActivity extends LocationUpdatingAppCompatActivity {

    private UserRecyclerAdapter userRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        userRecyclerAdapter = new UserRecyclerAdapter(recyclerView, currentUser, database, storage);
        recyclerView.setAdapter(userRecyclerAdapter);

        ((RadioGroup) findViewById(R.id.user_type)).setOnCheckedChangeListener(this::setUserTypeOnCheckedChangeListener);
        ((RadioGroup) findViewById(R.id.user_distance)).setOnCheckedChangeListener(this::setUserDistanceOnCheckedChangeListener);
        ((CheckBox) findViewById(R.id.active_users)).setOnCheckedChangeListener(this::setActiveUsersOnCheckedChangeListener);

        String findWalk = getIntent().getStringExtra("find_walk"); // "walkers", "owners", "none"
        if (findWalk.equals("walkers")) {
            ((RadioButton) findViewById(R.id.dog_walkers)).setChecked(true);
            ((RadioButton) findViewById(R.id.nearby)).setChecked(true);
            ((CheckBox) findViewById(R.id.active_users)).setChecked(true);
        } else if (findWalk.equals("owners")) {
            ((RadioButton) findViewById(R.id.dog_owners)).setChecked(true);
            ((RadioButton) findViewById(R.id.nearby)).setChecked(true);
            ((CheckBox) findViewById(R.id.active_users)).setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRecyclerAdapter.removeListener();
    }

    private void setUserTypeOnCheckedChangeListener(RadioGroup group, int checkedId) {
        final int dogOwnersId = R.id.dog_owners;
        final int dogWalkersId = R.id.dog_walkers;
        final int bothId = R.id.both;
        switch (checkedId) {
            case dogOwnersId:
                // TODO: filter in dog owners; filter out dog walkers
                Toast.makeText(this, "Dog Owners", Toast.LENGTH_SHORT).show();
                break;
            case dogWalkersId:
                // TODO: filter in dog walkers; filter out dog owners
                Toast.makeText(this, "Dog Walkers", Toast.LENGTH_SHORT).show();
                break;
            case bothId:
                // TODO: filter in both dog walkers and dog owners
                Toast.makeText(this, "Both", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setUserDistanceOnCheckedChangeListener(RadioGroup group, int checkedId) {
        final int nearby = R.id.nearby;
        final int anywhere = R.id.anywhere;
        switch (checkedId) {
            case nearby:
                // TODO: filter out users that are not nearby
                Toast.makeText(this, "Nearby", Toast.LENGTH_SHORT).show();
                break;
            case anywhere:
                // TODO: filter in users that are not nearby
                Toast.makeText(this, "Anywhere", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setActiveUsersOnCheckedChangeListener(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            // TODO: filter out non-active users
            Toast.makeText(this, "Active Users - true", Toast.LENGTH_SHORT).show();
        } else {
            // TODO: filter in non-active users
            Toast.makeText(this, "Active Users - false", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_users);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO: filter current list
                return true;
            }
        });

        return true;
    }

    /* TODO:
        When user clicks another profile picture -> display the user's profile.
            - User profile contains all user & dog info, plus buttons to send message, add contact (with Snack to cancel),
              and request walk (with dialog to set time/Snack to cancel)
        When a user clicks on a "request walk" button -> request to start a walk with a Snack to cancel the action. */

    @Override
    protected void setGeoQuery(Location location) {
        super.setGeoQuery(location);
        userRecyclerAdapter.setLocationListener(geoQuery);
    }
}