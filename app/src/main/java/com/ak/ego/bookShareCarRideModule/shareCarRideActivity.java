


package com.ak.ego.bookShareCarRideModule;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

import com.ak.ego.R;
import com.ak.ego.gps_tracker.GPSTracker;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class shareCarRideActivity extends AppCompatActivity {
    private String TAG = "SHARECARRIDEACTIVITY";
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView drawer_name;
    private Toolbar toolbar;
    private GoogleMap mMap1;
    private GPSTracker gpsTracker1;
    private GoogleMap mMap2;
    private GPSTracker gpsTracker2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_car_ride_activity_layout);

        /* this code is responsible for rendering the drawer with its callbacks */

        drawer_name = (TextView)findViewById(R.id.drawer_name);
        drawer_name.setText("Ankush Khurana");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            final ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setDisplayShowTitleEnabled(true);
                actionBar.setDisplayUseLogoEnabled(false);
                actionBar.setHomeButtonEnabled(true);
            }
        }
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawer.addDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        /* -  - */

        /* this code is now responsible for launching the search api for the places */

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(12.0f);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                autocompleteFragment.setText(place.getName()+" "+place.getAddress());
                Log.i(TAG, "Place: " + place.getName()+" "+place.getAddress());
                LatLng location = place.getLatLng();
                mMap1.addMarker(new MarkerOptions().position(location).title("My Location"));
                mMap1.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        final PlaceAutocompleteFragment endPlaceAutoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.end_place_autocomplete_fragment);
        ((EditText)endPlaceAutoCompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(12.0f);

        endPlaceAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.

                endPlaceAutoCompleteFragment.setText(place.getName()+" "+place.getAddress());
                Log.i(TAG, "Place: " + place.getName()+""+place.getAddress());
                LatLng location = place.getLatLng();
                mMap2.addMarker(new MarkerOptions().position(location).title("My Location"));
                mMap2   .moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        /* - - */
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            gpsTracker1 = new GPSTracker(this);
            gpsTracker2 = new GPSTracker(this);

        }
        SupportMapFragment mapFragment1 = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map1);
        mapFragment1.getMapAsync(onMapReadyCallback1());

        SupportMapFragment mapFragment2 = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment2.getMapAsync(onMapReadyCallback2());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_share_card_ride_activity, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();

    }


    public OnMapReadyCallback onMapReadyCallback1(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap1 = googleMap;
                if (gpsTracker1!= null && gpsTracker1.getIsGPSTrackingEnabled())
                {
                    String stringLatitude = String.valueOf(gpsTracker1.latitude);
                    Log.d("Latitiude",stringLatitude);
                    String stringLongitude = String.valueOf(gpsTracker1.longitude);
                    Log.d("Latitiude",stringLongitude);
                    LatLng location = new LatLng(Double.parseDouble(stringLatitude), Double.parseDouble(stringLongitude));
                    mMap1.addMarker(new MarkerOptions().position(location).title("My Location"));
                    mMap1.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

                }
                else if(gpsTracker1!= null)
                {
                    gpsTracker1.showSettingsAlert();
                }
            }
        };
    }


    public OnMapReadyCallback onMapReadyCallback2(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap2 = googleMap;
                if (gpsTracker2!= null && gpsTracker2.getIsGPSTrackingEnabled())
                {
                    String stringLatitude = String.valueOf(gpsTracker2.latitude);
                    Log.d("Latitiude",stringLatitude);
                    String stringLongitude = String.valueOf(gpsTracker2.longitude);
                    Log.d("Latitiude",stringLongitude);
                    LatLng location = new LatLng(Double.parseDouble(stringLatitude), Double.parseDouble(stringLongitude));
                    mMap2.addMarker(new MarkerOptions().position(location).title("My Location"));
                    mMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

                }
                else if(gpsTracker2!= null)
                {
                    gpsTracker2.showSettingsAlert();
                }
            }
        };
    }

}