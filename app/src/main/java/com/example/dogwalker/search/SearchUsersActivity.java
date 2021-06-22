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
        userRecyclerAdapter = new UserRecyclerAdapter(recyclerView, currentUser, database, storage, geoQuery);
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
        } else {

        }
    }

    @Override
    protected void setGeoQuery(Location location) {
        super.setGeoQuery(location);
        userRecyclerAdapter.setLocationListener();
    }

    private void setUserTypeOnCheckedChangeListener(RadioGroup group, int checkedId) {
        final int dogOwnersId = R.id.dog_owners;
        final int dogWalkersId = R.id.dog_walkers;
        final int eitherId = R.id.either;
        switch (checkedId) {
            case dogOwnersId:
                Toast.makeText(this, "Dog Owners", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserType(Filters.UserType.DOG_OWNERS);
                break;
            case dogWalkersId:
                Toast.makeText(this, "Dog Walkers", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserType(Filters.UserType.DOG_WALKERS);
                break;
            case eitherId:
                Toast.makeText(this, "Either", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserType(Filters.UserType.EITHER);
                break;
        }
    }

    private void setUserDistanceOnCheckedChangeListener(RadioGroup group, int checkedId) {
        final int nearby = R.id.nearby;
        final int anywhere = R.id.anywhere;
        switch (checkedId) {
            case nearby:
                Toast.makeText(this, "Nearby", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserDistance(Filters.UserDistance.NEARBY);
                break;
            case anywhere:
                Toast.makeText(this, "Anywhere", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserDistance(Filters.UserDistance.ANYWHERE);
                break;
        }
    }

    private void setActiveUsersOnCheckedChangeListener(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(this, "Active Users - true", Toast.LENGTH_SHORT).show();
            userRecyclerAdapter.setActiveUsers(Filters.ActiveUsers.ACTIVE_USERS);
        } else {
            Toast.makeText(this, "Active Users - false", Toast.LENGTH_SHORT).show();
            userRecyclerAdapter.setActiveUsers(Filters.ActiveUsers.ANY_USERS);
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
    protected void onDestroy() {
        super.onDestroy();
        userRecyclerAdapter.removeListeners();
    }
}