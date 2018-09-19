package com.ak.ego.share_vehicle_module;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ak.ego.AppConfig;
import com.ak.ego.AppController;
import com.ak.ego.R;
import com.ak.ego.gps_tracker.GPSTracker;
import com.ak.ego.recyclerViewItemClickListener;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.network.DirectionService;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class share_vehicle_activity extends AppCompatActivity implements LocationListener {

    private String TAG="ADDNEWACTIVITY";
    private AppConfig appConfig;
    private LocationManager locationManager;
    /*  config of the share details page */

    /* -------- */


    /* config of the drawer which is visible on sliding on the screen  */

    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView drawer_name;
    private android.support.v7.widget.Toolbar toolbar;

    /* ------- */

    /* config of the all users vehicles recycler view  */

    private RecyclerView my_vehicles_recycler;
    private vehicles_present_adpater vehicles_recycler_adapter;
    private ArrayList<Vehicle> vehicles = new ArrayList<>();

    /* ------- */


    private ImageView nothing_found_in_record_image;

    /* config of google maps */

    private GoogleMap mMap1;
    private GPSTracker gpsTracker1;
    private GoogleMap mMap2;
    private GPSTracker gpsTracker2;
    private MarkerOptions map1MarkerOption;
    private MarkerOptions map2MarkerOption;
    private Marker map1Marker;
    private Marker map2Marker;

    /* ------- */

    /* config of the confirm sharing details page */
        private TextView account_name;
        private TextView account_email;
        private TextView account_type;
        private TextView ride_vehicle;
        private TextView ride_vehicle_number;
        private TextView ride_vehicle_registration_number;
        private Spinner ride_type;
        private RelativeLayout confirmation_ride_layout;
        private ImageView back_confirmation_layout_button;
        private RelativeLayout start_location_layout;
        private RelativeLayout end_location_layout;
        private RelativeLayout no_location_selected_layout;
        private Button start_share ;
    /* ------- */

    /* code for service intent */
    LatLng location_map1;
    LatLng location_map2;
    /* ------ */

    /* code for routes selection */
    private ArrayList<Route> routes_to_destination = new ArrayList<>();
    private RecyclerView routes_selection_recycler_view;
    private RelativeLayout routes_selection_layout;
    private GoogleMap map_route_selector;
    private MarkerOptions marker_option_map_route_selector;
    private Marker start_marker;
    private Marker end_marker;
    private ImageView back_route_selection_layout;
    private select_route_adapter adapter_for_route_selection;
    private Place start_place;
    private Place end_place;
    private Marker start_location_marker;
    private Marker end_location_marker;
    private MarkerOptions start_location_marker_options;
    private MarkerOptions end_locaiton_marker_optiions;
    private LatLngBounds.Builder bounds_builder = new LatLngBounds.Builder();
    private LatLngBounds bounds ;
    /* ------------------ */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_vehicle_activity_layout);
        appConfig = new AppConfig(getApplicationContext());
        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);

        /* this code add the side bar and tool bar to the activity */

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

        /* -------------- */

        /* recycler view setup here this recycler view renders all the vehicles of the user */

        recyclerViewItemClickListener listner_vehicle_selection = (view, position)->{
            Vehicle vehicle_selected = vehicles.get(position);
            account_name.setText(appConfig.getUser_name());
            account_email.setText(appConfig.getUser_email());
            account_type.setText("Service Provider");

            ride_vehicle_number.setText(vehicle_selected.getNumber());
            ride_vehicle.setText(vehicle_selected.getType());
            ride_vehicle_registration_number.setText(vehicle_selected.getRegistration_number());
            confirmation_ride_layout.setVisibility(View.VISIBLE);
        };
        my_vehicles_recycler =  (RecyclerView)findViewById(R.id.my_vehicles_recycler);
        vehicles_recycler_adapter = new vehicles_present_adpater(this, vehicles, listner_vehicle_selection);
        my_vehicles_recycler.setHasFixedSize(true);
        my_vehicles_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        my_vehicles_recycler.setAdapter(vehicles_recycler_adapter);
        vehicles_recycler_adapter.notifyDataSetChanged();

        /* ------- */

        nothing_found_in_record_image = (ImageView)findViewById(R.id.nothing_found_image);
        new get_all_vehicles().execute();

        /*
        * this code now initialises the maps for selection of location to share
        */

        final PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                start_place = place;
                adapter_for_route_selection.start_location = start_place.getName()+"  "+start_place.getAddress();
                autocompleteFragment.setText(place.getName()+" "+place.getAddress());
                Log.i(TAG, "Place: " + place.getName()+" "+place.getAddress());
                 location_map1 = place.getLatLng();
                map1MarkerOption = new MarkerOptions();
                map1MarkerOption.position(location_map1);
                map1MarkerOption.title("My Location");
                if(map1Marker == null)
                {
                    map1Marker = mMap1.addMarker(map1MarkerOption);
                }
                map1Marker.setPosition(location_map1);
                mMap1.moveCamera(CameraUpdateFactory.newLatLngZoom(location_map1, 18.0f));

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        final PlaceAutocompleteFragment endPlaceAutoCompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.end_place_autocomplete_fragment);

        endPlaceAutoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                end_place = place;
                adapter_for_route_selection.end_location = end_place.getName()+"  "+end_place.getAddress();
                endPlaceAutoCompleteFragment.setText(place.getName()+" "+place.getAddress());
                Log.i(TAG, "Place: " + place.getName()+""+place.getAddress());
                location_map2 = place.getLatLng();
                map2MarkerOption = new MarkerOptions();
                map2MarkerOption.position(location_map2);
                map2MarkerOption.title("My Location");
                if(map2Marker == null)
                {
                    map2Marker = mMap2.addMarker(map2MarkerOption);
                }
                map2Marker.setPosition(location_map2);
                mMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(location_map2, 18.0f));

            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


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

        ((EditText)endPlaceAutoCompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(12.0f);
        ((EditText)autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(10.0f);

        /* ---------- */

        /* confirmation form configuratins */

        account_name = (TextView)findViewById(R.id.account_name);
        account_email = (TextView)findViewById(R.id.account_email);
        account_type = (TextView)findViewById(R.id.account_acting_type);

        ride_vehicle_number = (TextView)findViewById(R.id.ride_vehicle_number);
        ride_vehicle = (TextView)findViewById(R.id.ride_vehicle_type);
        ride_type = (Spinner)findViewById(R.id.ride_type);
        ride_vehicle_registration_number =  (TextView)findViewById(R.id.ride_registration_number);

        back_confirmation_layout_button = (ImageView)findViewById(R.id.back_share_vehicle_confirm_layout);
        back_confirmation_layout_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmation_ride_layout.setVisibility(View.INVISIBLE);
            }
        });
        start_share = (Button)findViewById(R.id.start_share);
        start_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        confirmation_ride_layout = (RelativeLayout)findViewById(R.id.confirmation_ride_layout);
        confirmation_ride_layout.setVisibility(View.INVISIBLE);
        start_location_layout = (RelativeLayout)findViewById(R.id.start_location_layout);
        end_location_layout = (RelativeLayout)findViewById(R.id.end_location_layout);
        no_location_selected_layout = (RelativeLayout)findViewById(R.id.no_location_selected_layout);

        ArrayList<String> selection_options = new ArrayList<>();
        selection_options.add("From selected start and end location");
        selection_options.add("From any start location to end location");
        selection_options.add("From selected start location to any end location");
        selection_options.add("From any start location to selected end location");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner_item,selection_options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        ride_type.setAdapter(adapter);
        ride_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(selection_options.get(position).equals("From selected start and end location"))
                {
                    end_location_layout.setVisibility(View.VISIBLE);
                    start_location_layout.setVisibility(View.VISIBLE);
                    no_location_selected_layout.setVisibility(View.INVISIBLE);
                }
                else if(selection_options.get(position).equals("From any start location to end location"))
                {
                    end_location_layout.setVisibility(View.GONE);
                    start_location_layout.setVisibility(View.GONE);
                    no_location_selected_layout.setVisibility(View.VISIBLE);
                }
                else if(selection_options.get(position).equals("From selected start location to any end location"))
                {
                    start_location_layout.setVisibility(View.VISIBLE);
                    end_location_layout.setVisibility(View.GONE);
                    no_location_selected_layout.setVisibility(View.INVISIBLE);
                }
                else if(selection_options.get(position).equals("From any start location to selected end location"))
                {
                    start_location_layout.setVisibility(View.GONE);
                    end_location_layout.setVisibility(View.VISIBLE);
                    no_location_selected_layout.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        start_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                get_directions();
            }
        });
        /* ----------------- */
        request_locations();

        /* code for location selector recycler */

        routes_selection_layout = (RelativeLayout)findViewById(R.id.route_selection_recycler_layout);
        routes_selection_recycler_view = (RecyclerView)findViewById(R.id.locations_for_selection_recycler);
        routes_selection_recycler_view.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        routes_selection_recycler_view.setHasFixedSize(true);
        recyclerViewItemClickListener location_selected_listener = (view, position)->{
            Toast.makeText(getApplicationContext(),"you have selected location at position"+position,Toast.LENGTH_SHORT).show();
        };
        adapter_for_route_selection = new select_route_adapter(this,routes_to_destination,location_selected_listener,"","");
        routes_selection_recycler_view.setAdapter(adapter_for_route_selection);
        adapter_for_route_selection.notifyDataSetChanged();
        back_route_selection_layout = (ImageView)findViewById(R.id.back_select_travel_route);
        back_route_selection_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routes_selection_layout.setVisibility(View.INVISIBLE);
            }
        });
        /* ------- */
    }


    /* code now is responsible for directions */
     public void get_directions()
     {
         GoogleDirection.withServerKey(getString(R.string.api_key))
                 .from(new LatLng(location_map1.latitude, location_map1.longitude))
                 .to(new LatLng(location_map2.latitude, location_map2.longitude))
                 .transportMode(TransportMode.DRIVING)
                 .alternativeRoute(true)
                 .unit(Unit.METRIC)
                 .execute(new DirectionCallback() {
                     @Override
                     public void onDirectionSuccess(Direction direction, String rawBody) {
                         routes_selection_layout.setVisibility(View.VISIBLE);
                         routes_to_destination.clear();
                         map_route_selector.clear();
                         if(start_location_marker != null)
                         {
                             LatLng start_location = start_place.getLatLng();
                             start_location_marker.setPosition(start_location);
                             start_location_marker_options.position(start_location);
                             map_route_selector.addMarker(start_location_marker_options);
                             map_route_selector.moveCamera(CameraUpdateFactory.newLatLngZoom(start_location, 12.0f));
                         }
                         else
                         {
                             LatLng start_location = start_place.getLatLng();
                             start_location_marker_options = new MarkerOptions();
                             start_location_marker_options.title("Your start location");
                             start_location_marker_options.position(start_location);
                             start_location_marker = map_route_selector.addMarker(start_location_marker_options);
                             start_location_marker.setPosition(start_location);
                            map_route_selector.moveCamera(CameraUpdateFactory.newLatLngZoom(start_location,12.0f));
                         }

                         if(end_location_marker != null)
                         {
                             LatLng end_location = end_place.getLatLng();
                             end_location_marker.setPosition(end_location);
                             end_locaiton_marker_optiions.position(end_location);
                             map_route_selector.addMarker(end_locaiton_marker_optiions);
                             map_route_selector.moveCamera(CameraUpdateFactory.newLatLngZoom(end_location, 12.0f));
                         }
                         else
                         {

                             LatLng end_location = end_place.getLatLng();
                             end_locaiton_marker_optiions = new MarkerOptions();
                             end_locaiton_marker_optiions.title("Your end location");
                             end_locaiton_marker_optiions.position(end_location);
                             end_location_marker = map_route_selector.addMarker(end_locaiton_marker_optiions);
                             end_location_marker.setPosition(end_location);
                             map_route_selector.moveCamera(CameraUpdateFactory.newLatLngZoom(end_location,12.0f));
                         }
                         bounds_builder.include(start_location_marker.getPosition());
                         bounds_builder.include(end_location_marker.getPosition());
                         bounds = bounds_builder.build();
                         map_route_selector.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,0));

                         adapter_for_route_selection.notifyDataSetChanged();
                         if(direction.isOK()) {
                            List<Route> routes =  direction.getRouteList();
                            for(int i =0;i<routes.size();i++)
                            {
                                if(i == 0)
                                {
                                    ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.GREEN);
                                    map_route_selector.addPolyline(polylineOptions);
                                }
                                else if(i == 1)
                                {
                                    ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.YELLOW);
                                    map_route_selector.addPolyline(polylineOptions);

                                }
                                else if(i == 2)
                                {
                                    ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                                    map_route_selector.addPolyline(polylineOptions);

                                }
                                else
                                {
                                    ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                    PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.DKGRAY);
                                    map_route_selector.addPolyline(polylineOptions);
                                }
                                Log.d("Route",routes.get(i).getSummary().toString());
                                routes_to_destination.add(routes.get(i));
                                adapter_for_route_selection.notifyDataSetChanged();

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

    /* ----------------- */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_share_card_ride_activity, menu);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location Changed",location.getLongitude()+" "+location.getLatitude());
        LatLng coordinates = new LatLng(location.getLatitude(),location.getLongitude());
        if(map1Marker != null)
        {
            map1Marker.setPosition(coordinates);
            mMap1.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void request_locations(){
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        String provider = locationManager.getBestProvider(criteria,true);
        locationManager.requestLocationUpdates(provider,10000,10,this);
    }

    public class get_all_vehicles extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "url requested: "+AppConfig.get_all_users_vehicles+appConfig.getUser_email());
            JsonArrayRequest request = new JsonArrayRequest(AppConfig.get_all_users_vehicles+appConfig.getUser_email().trim(), new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Toast.makeText(getApplicationContext()  ,""+response.toString(),Toast.LENGTH_SHORT).show();
                    try{
                        for(int i = 0; i<response.length();i++)
                        {
                            JSONObject response_object = response.getJSONObject(i);
                            Vehicle vh = new Vehicle();
                            vh.setNumber(response_object.getString("number"));
                            vh.setCity(response_object.getString("city"));
                            vh.setState(response_object.getString("state"));
                            vh.setType(response_object.getString("vehicle_type"));
                            vh.setRegistration_number(response_object.getString("registration_number"));
                            vehicles.add(vh);
                            vehicles_recycler_adapter.notifyDataSetChanged();
                        }
                    }catch (Exception e)
                    {
                        Toast.makeText(getApplicationContext(),"Could not load vehicles",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    if(vehicles.size() > 0)
                    {
                        nothing_found_in_record_image.setVisibility(View.INVISIBLE);
                        my_vehicles_recycler.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        nothing_found_in_record_image.setVisibility(View.VISIBLE);
                        my_vehicles_recycler.setVisibility(View.INVISIBLE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("Volley Error",error.toString());
                    Toast.makeText(getApplicationContext(),"Some issue with system",Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                public Map<String,String> getParams(){
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("email",appConfig.getUser_email());
                    return params;
                }
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


    /* methods below initialise the maps */

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
                    map1MarkerOption = new MarkerOptions();
                    map1MarkerOption.position(location);
                    map1MarkerOption.title("My Current Location");
                    if(map1Marker == null)
                    {
                        map1Marker = mMap1.addMarker(map1MarkerOption);
                    }
                    map1Marker.setPosition(location);
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
                    map2MarkerOption = new MarkerOptions();
                    map2MarkerOption.position(location);
                    map2MarkerOption.title("My Current Location");
                    if(map2Marker == null)
                    {
                        map2Marker = mMap2.addMarker(map2MarkerOption);
                    }
                    map2Marker.setPosition(location);
                    mMap2.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

                }
                else if(gpsTracker2!= null)
                {
                    gpsTracker2.showSettingsAlert();
                }
            }
        };
    }

    /* ------------- */


    /* map for routes selection */

    public OnMapReadyCallback route_selection_map_ready_callback(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map_route_selector = googleMap;
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
                        start_location_marker = map_route_selector.addMarker(start_location_marker_options);
                    }
                    start_location_marker.setPosition(location);
                    map_route_selector.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

                }
                else if(gpsTracker1!= null)
                {
                    gpsTracker1.showSettingsAlert();
                }
            }
        };
    }
    /* ------------- */


}
