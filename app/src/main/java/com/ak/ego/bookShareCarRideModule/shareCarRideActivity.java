


package com.ak.ego.bookShareCarRideModule;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ak.ego.R;
import com.ak.ego.gps_tracker.GPSTracker;
import com.ak.ego.recyclerViewItemClickListener;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class shareCarRideActivity extends AppCompatActivity {
    private String TAG = "SHARECARRIDEACTIVITY";
    private Toolbar toolbar;
    private GoogleMap mMap1;
    private GPSTracker gpsTracker1;
    private GoogleMap mMap2;
    private GPSTracker gpsTracker2;
    private Place start_place;
    private Place destination_place;
    private Button confirm_locations_proceed;

    // code below includes all about displaying the routes //

    private RelativeLayout select_route_recycler_layout;
    private RecyclerView routes_to_select_recycler;
    private routes_selection_adapter route_to_select_adapter;
    private String start_location_address = new String();
    private String end_location_address = new String();
    private ArrayList<Route> routes_to_select_list = new ArrayList<>();
    private Route route_selected_to_move_onn;
    private GoogleMap map_to_render_direction_polylines;
    private Marker start_location_marker;
    private MarkerOptions start_location_marker_options;
    private Marker end_location_marker;
    private MarkerOptions end_location_marker_options;
    private LatLngBounds.Builder bounds_builder = new LatLngBounds.Builder();
    private LatLngBounds bounds ;
    private ImageView back_route_selection;
    private ImageView confirm_ride_route;
    // ---------- //

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_share_car_ride_activity_layout);

        /*  code below is responsible for searching the directions to start and end location */

        confirm_locations_proceed = (Button)findViewById(R.id.confirm_location_proceed);
        confirm_locations_proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(start_place == null || destination_place == null)
                {
                   Toast.makeText(getApplicationContext(),"Places are not selected please select before proceeding",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    get_directions();
                }
            }
        });

        /* ------------------- */

        /* code below is responsible for rendering the route selection layout */
            recyclerViewItemClickListener route_selection_listener = (view, position)->{
                route_selected_to_move_onn = routes_to_select_list.get(position);
                Toast.makeText(getApplicationContext(),"Route Selected Please Proceed ",Toast.LENGTH_SHORT).show();
            };
            select_route_recycler_layout = (RelativeLayout)findViewById(R.id.route_selection_recycler_layout);
            routes_to_select_recycler = (RecyclerView)findViewById(R.id.locations_for_selection_recycler);
            routes_to_select_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            routes_to_select_recycler.setHasFixedSize(true);
            route_to_select_adapter = new routes_selection_adapter(this,routes_to_select_list,route_selection_listener,start_location_address,end_location_address);
            routes_to_select_recycler.setAdapter(route_to_select_adapter);
            route_to_select_adapter.notifyDataSetChanged();
            back_route_selection = (ImageView)findViewById(R.id.back_select_travel_route);
            back_route_selection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    slideToBottom(select_route_recycler_layout);
                }
            });
            confirm_ride_route = (ImageView)findViewById(R.id.confirm_and_start_my_ride_sharing);
            confirm_ride_route.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(),searchForRideActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("RouteToDestination",route_selected_to_move_onn);
                    intent.putExtra("start_place_latitude",start_place.getLatLng().latitude);
                    intent.putExtra("start_place_longitude",start_place.getLatLng().longitude);
                    intent.putExtra("end_place_latitude",destination_place.getLatLng().latitude);
                    intent.putExtra("end_place_longitude",destination_place.getLatLng().longitude);
                    intent.putParcelableArrayListExtra("direction_points",route_selected_to_move_onn.getLegList().get(0).getDirectionPoint());
                    startActivity(intent);
                }
            });
        /* ----------------- */

        /* this code is responsible for rendering the drawer with its callbacks */


//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            setSupportActionBar(toolbar);
//            final ActionBar actionBar = getSupportActionBar();
//            if (actionBar != null) {
//                actionBar.setDisplayHomeAsUpEnabled(true);
//                actionBar.setDisplayShowHomeEnabled(true);
//                actionBar.setDisplayShowTitleEnabled(true);
//                actionBar.setDisplayUseLogoEnabled(false);
//                actionBar.setHomeButtonEnabled(true);
//            }
//        }

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
                start_place = place;
                start_location_address = place.getAddress().toString();
                route_to_select_adapter.start_location = start_location_address;
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
                destination_place = place;
                end_location_address = place.getAddress().toString();
                route_to_select_adapter.end_location = end_location_address;
                mMap2.addMarker(new MarkerOptions().position(location).title("My Location"));
                mMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

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

        SupportMapFragment map_fragment_route_selector = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_presenting_locations);
        map_fragment_route_selector.getMapAsync(route_selection_map_ready_callback());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_share_card_ride_activity, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
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


    public void get_directions()
    {
        GoogleDirection.withServerKey(getString(R.string.api_key))
                    .from(new LatLng(start_place.getLatLng().latitude,start_place.getLatLng().longitude))
                .to(new LatLng(destination_place.getLatLng().latitude, destination_place.getLatLng().longitude))
                .transportMode(TransportMode.DRIVING)
                .alternativeRoute(true)
                .unit(Unit.METRIC)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        slideToTop(select_route_recycler_layout);
                        routes_to_select_list.clear();
                        map_to_render_direction_polylines.clear();
                        if(start_location_marker != null)
                        {
                            LatLng start_location = start_place.getLatLng();
                            start_location_marker.setPosition(start_location);
                            start_location_marker_options.position(start_location);
                            map_to_render_direction_polylines.addMarker(start_location_marker_options);
                            map_to_render_direction_polylines.moveCamera(CameraUpdateFactory.newLatLngZoom(start_location, 12.0f));
                        }
                        else
                        {
                            LatLng start_location = start_place.getLatLng();
                            start_location_marker_options = new MarkerOptions();
                            start_location_marker_options.title("Your start location");
                            start_location_marker_options.position(start_location);
                            start_location_marker = map_to_render_direction_polylines.addMarker(start_location_marker_options);
                            start_location_marker.setPosition(start_location);
                            map_to_render_direction_polylines.moveCamera(CameraUpdateFactory.newLatLngZoom(start_location,12.0f));
                        }

                        if(end_location_marker != null)
                        {
                            LatLng end_location = destination_place.getLatLng();
                            end_location_marker.setPosition(end_location);
                            end_location_marker_options.position(end_location);
                            map_to_render_direction_polylines.addMarker(end_location_marker_options);
                            map_to_render_direction_polylines.moveCamera(CameraUpdateFactory.newLatLngZoom(end_location, 12.0f));
                        }
                        else
                        {

                            LatLng end_location = destination_place.getLatLng();
                            end_location_marker_options = new MarkerOptions();
                            end_location_marker_options.title("Your end location");
                            end_location_marker_options.position(end_location);
                            end_location_marker = map_to_render_direction_polylines.addMarker(end_location_marker_options);
                            end_location_marker.setPosition(end_location);
                            map_to_render_direction_polylines.moveCamera(CameraUpdateFactory.newLatLngZoom(end_location,12.0f));
                        }
                        bounds_builder.include(start_location_marker.getPosition());
                        bounds_builder.include(end_location_marker.getPosition());
                        bounds = bounds_builder.build();
                        map_to_render_direction_polylines.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));

                        route_to_select_adapter.notifyDataSetChanged();
                        if(direction.isOK()) {
                            List<Route> routes =  direction.getRouteList();
                            for(int i =0;i<routes.size();i++)
                            {
                                if(i == 0)
                                {
                                    ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.GREEN);
                                    map_to_render_direction_polylines.addPolyline(polylineOptions);
                                }
                                else if(i == 1)
                                {
                                    ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.YELLOW);
                                    map_to_render_direction_polylines.addPolyline(polylineOptions);

                                }
                                else if(i == 2)
                                {
                                    ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                                    map_to_render_direction_polylines.addPolyline(polylineOptions);

                                }
                                else
                                {
                                    ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.DKGRAY);
                                    map_to_render_direction_polylines.addPolyline(polylineOptions);
                                }
                                Log.d("Route",routes.get(i).getSummary().toString());
                                routes_to_select_list.add(routes.get(i));
                                route_to_select_adapter.notifyDataSetChanged();

                            }
                            for(int i =0;i<routes.size();i++)
                            {
                                Log.d("Route",routes.get(i).getOverviewPolyline().toString());
                            }

                        } else {
                            Toast.makeText(getApplicationContext(),"Directions Not Recieved ",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        Toast.makeText(getApplicationContext(),"Failure in directions fetch please retry !!",Toast.LENGTH_SHORT).show();
                        // Do something
                    }
                });
        }

    public void slideToBottom(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,0,view.getHeight()-10);
        animate.setDuration(500);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) {view.setVisibility(View.INVISIBLE);}
            @Override public void onAnimationRepeat(Animation animation) { }
        });
        view.startAnimation(animate);
    }

    public void slideToTop(View view){
        TranslateAnimation animate = new TranslateAnimation(0,0,view.getHeight(),0);
        animate.setDuration(500);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) {view.setVisibility(View.VISIBLE); }
            @Override public void onAnimationEnd(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(animate);
    }

    public OnMapReadyCallback route_selection_map_ready_callback(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map_to_render_direction_polylines = googleMap;
                if (gpsTracker1!= null && gpsTracker1.getIsGPSTrackingEnabled())
                {
                    String stringLatitude = String.valueOf(gpsTracker1.latitude);
                    Log.d("Latitiude",stringLatitude);
                    String stringLongitude = String.valueOf(gpsTracker1.longitude);
                    Log.d("Latitiude",stringLongitude);
                    LatLng location = new LatLng(Double.parseDouble(stringLatitude), Double.parseDouble(stringLongitude));
                    start_location_marker_options = new MarkerOptions();
                    start_location_marker_options.title("Your start location");
                    start_location_marker_options.position(location);
                    if(start_location_marker == null)
                    {
                        start_location_marker = map_to_render_direction_polylines.addMarker(start_location_marker_options);
                    }
                    start_location_marker.setPosition(location);
                    map_to_render_direction_polylines.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

                }
                else if(gpsTracker1!= null)
                {
                    gpsTracker1.showSettingsAlert();
                }
            }
        };
    }
}