package com.example.dogwalker.search;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.widget.SearchView;

import com.example.dogwalker.LocationUpdatingAppCompatActivity;
import com.example.dogwalker.R;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class SearchActivity extends LocationUpdatingAppCompatActivity {

    private boolean findWalk = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String findWalkExtra = getIntent().getStringExtra("find_walk"); // "walkers", "owners", "none"

        if (findWalkExtra.equals("walkers")) {
            findWalk = true;
            // TODO: set configs to search for local active walkers only
        } else if (findWalkExtra.equals("owners")) {
            findWalk = true;
            // TODO: set configs to search for local active owners only
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        Log.d("menu", "inflater = " + inflater);
        inflater.inflate(R.menu.menu_toolbar_search, menu);
        Log.d("menu", "menu = " + menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_users);
        Log.d("menu", "searchItem = " + searchItem);
        SearchView searchView = (SearchView) searchItem.getActionView();
        Log.d("menu", "searchView = " + searchView);

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
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {

            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }
}