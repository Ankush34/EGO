package com.ak.ego.mainActivityModule;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ak.ego.AppConfig;
import com.ak.ego.AppController;
import com.ak.ego.R;
import com.ak.ego.share_vehicle_module.share_vehicle_activity;
import com.ak.ego.gps_tracker.GPSTracker;
import com.ak.ego.shareCarRideModule.shareCarRideActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class mainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private String city;
    private String country;
    private String postalCode;
    private String addressLine;
    private TextView location_city;
    private TextView location_country;
    private TextView location_code;
    private TextView location_address;
    private CardView book_a_share_ride;
    private CardView share_my_vehicle;
    private AppConfig appConfig;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appConfig = new AppConfig(getApplicationContext());
        location_city = (TextView)findViewById(R.id.location_city);
        location_country = (TextView)findViewById(R.id.location_country);
        location_code = (TextView)findViewById(R.id.location_postal_code);
        location_address = (TextView)findViewById(R.id.location_address_line);
        share_my_vehicle = (CardView)findViewById(R.id.share_my_vehicle);
        share_my_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),share_vehicle_activity.class);
                startActivity(intent);
            }
        });
        book_a_share_ride = (CardView)findViewById(R.id.book_a_share_ride);
        book_a_share_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainActivity.this,shareCarRideActivity.class);
                startActivity(intent);
            }
        });
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            gpsTracker = new GPSTracker(this);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        new get_all_users_test().execute();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (gpsTracker!= null && gpsTracker.getIsGPSTrackingEnabled())
        {
            String stringLatitude = String.valueOf(gpsTracker.latitude);
            Log.d("Latitiude",stringLatitude);
            String stringLongitude = String.valueOf(gpsTracker.longitude);
            Log.d("Latitiude",stringLongitude);
            try {
                country = gpsTracker.getCountryName(this);
                location_country.setText("Country:  "+country);
                city = gpsTracker.getLocality(this);
                location_city.setText("City:  "+city);
                postalCode = gpsTracker.getPostalCode(this);
                location_code.setText("Postal Code:  "+postalCode);
                addressLine = gpsTracker.getAddressLine(this);
                location_address.setText("Address:  "+addressLine);
            }catch (Exception e)
            {
                Toast.makeText(getApplicationContext(),"Could Not Get Your Location ",Toast.LENGTH_SHORT).show();
                location_country.setText("Country:  "+"");
                location_city.setText("City:  "+"");
                location_code.setText("Postal Code:  "+"");
                location_address.setText("Address:  "+"");

            }
            LatLng location = new LatLng(Double.parseDouble(stringLatitude), Double.parseDouble(stringLongitude));
            mMap.addMarker(new MarkerOptions().position(location).title("My Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

        }
        else if(gpsTracker!= null)
        {
            gpsTracker.showSettingsAlert();
        }
        // Add a marker in Sydney, Australia, and move the camera.
    }
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    gpsTracker = new GPSTracker(this);
                    onMapReady(mMap);
                } else {
                    Toast.makeText(getApplicationContext(),"Sorry we can not proceed furthur without locaiton permission ", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public class get_all_users_test extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            JsonArrayRequest request = new JsonArrayRequest(AppConfig.get_all_users, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Toast.makeText(getApplicationContext()  ,""+response.toString(),Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Volley Error",error.toString());
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json; charset=UTF-8");
                    params.put("Authorization","Bearer " + appConfig.getBearerToken());
                    return params;
                }
            };
            AppController.getInstance().addToRequestQueue(request);
            return  null;
        }
    }
    @Override
    protected void onResume() {

        super.onResume();
    }
}
