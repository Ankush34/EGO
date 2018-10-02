package com.ak.ego.bookShareCarRideModule;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ak.ego.AppConfig;
import com.ak.ego.R;
import com.ak.ego.gps_tracker.GPSTracker;
import com.ak.ego.recyclerViewItemClickListener;
import com.ak.ego.share_vehicle_module.LocationService;
import com.ak.ego.share_vehicle_module.SharingUserModule.userProfile;
import com.ak.ego.share_vehicle_module.screenCustomViewMobule.ArcLayout;
import com.ak.ego.share_vehicle_module.start_share_service_activity;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2Cap;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2RegionCoverer;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.ui.IconGenerator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class searchForRideActivity extends AppCompatActivity implements OnMapReadyCallback {
    private FragmentPagerAdapter adapterViewPager;

    private GoogleMap map_show_riders;
    private GPSTracker gpsTracker;
    private MarkerOptions startMarkerOptions1;
    private MarkerOptions endMarkerOptions2;
    private Marker start_marker;
    private Marker end_marker;
    private Intent intent;
    private AppConfig appConfig;

    private ArrayList<String> user_visible_on_map_ids = new ArrayList<>();
    private bookingShareCabServiceBroadcaseReceiver broadcaseReceiver;

    private RelativeLayout rides_available_layout;
    private BottomSheetBehavior bottomSheetBehavior ;
    private ArcLayout curve_layout;
    private ImageView back_select_ride;

    private LinearLayout layout_main_back;

    private RecyclerView view_show_drivers_available;
    private providers_available_adapter show_drivers_adapter;
    private ArrayList<userProfile> drivers_present_to_provide_rides = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_driver_layout_sharing_ride_layout);

        ViewPager vpPager = (ViewPager) findViewById(R.id.vpPager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        vpPager.setAdapter(adapterViewPager);

        view_show_drivers_available = (RecyclerView)findViewById(R.id.rides_recycler);
        view_show_drivers_available.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        view_show_drivers_available.setHasFixedSize(true);

        recyclerViewItemClickListener listener_driver_selection = (view, position)->
        {
            Toast.makeText(getApplicationContext(),"Driver Selected",Toast.LENGTH_SHORT).show();
        };
        show_drivers_adapter = new  providers_available_adapter(this, drivers_present_to_provide_rides,listener_driver_selection);
        view_show_drivers_available.setAdapter(show_drivers_adapter);
        show_drivers_adapter.notifyDataSetChanged();

        layout_main_back = (LinearLayout)findViewById(R.id.layout_main_back);

        rides_available_layout = (RelativeLayout)findViewById(R.id.rides_available_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(rides_available_layout);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
                        if(drivers_present_to_provide_rides.size() > 0)
                        {
                            show_drivers_adapter.notifyDataSetChanged();
                        }
                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        curve_layout = (ArcLayout)findViewById(R.id.curve_layout);
        curve_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                return true;
            }
        });

        layout_main_back.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int Y = (int) event.getRawY();
                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                return true;
            }
        });

        back_select_ride = (ImageView)findViewById(R.id.back_select_riding_user);
        back_select_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        appConfig = new AppConfig(getApplicationContext());
        broadcaseReceiver = new bookingShareCabServiceBroadcaseReceiver();
        /*  below code is responsible for getting the car selection views in the */

        recyclerViewItemClickListener listener_class_selector = (view, position)->{

        };

        /* ------------- */
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        else
        {
            gpsTracker = new GPSTracker(this);

        }
        SupportMapFragment mapFragment1 = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment1.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map_show_riders = googleMap;
        if (gpsTracker!= null && gpsTracker.getIsGPSTrackingEnabled())
        {
            LatLng start_location = new LatLng(getIntent().getDoubleExtra("start_place_latitude",0), getIntent().getDoubleExtra("start_place_longitude",0));
            LatLng end_location = new LatLng(getIntent().getDoubleExtra("end_place_latitude",0),getIntent().getDoubleExtra("end_place_longitude",0));

            startMarkerOptions1 = new MarkerOptions();
            startMarkerOptions1.position(start_location);
            start_marker = map_show_riders.addMarker(startMarkerOptions1);
            start_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.start_location));
            start_marker.setPosition(start_location);

            endMarkerOptions2 = new MarkerOptions();
            endMarkerOptions2.title("End Location");
            endMarkerOptions2.position(end_location);
            end_marker = map_show_riders.addMarker(endMarkerOptions2);
            end_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.end_location));
            end_marker.setPosition(end_location);

            map_show_riders.moveCamera(CameraUpdateFactory.newLatLngZoom(start_location,14.5f));
            ArrayList<LatLng> directionPositionList = getIntent().getParcelableArrayListExtra("direction_points");
            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 8, Color.DKGRAY);
            map_show_riders.addPolyline(polylineOptions);

        }
        else if(gpsTracker!= null)
        {
            gpsTracker.showSettingsAlert();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        @SuppressLint("HandlerLeak") Handler share_activity_hander_by_message = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d("HANDLERACTIVITY", "i received");
                Bundle data_received = msg.getData();
                JSONArray array = (JSONArray) msg.obj;
                Log.d("JSONOBJECTRESPONSE", array.toString());
                try {
                    for (int i = 0; i < array.length(); i++) {

                        JSONObject object = array.getJSONObject(i);
                        if (!user_visible_on_map_ids.contains(object.getString("id"))) {

                            userProfile userProfile = new userProfile();
                            userProfile.setName(object.getString("name"));
                            userProfile.setUser_id(object.getString("id"));
                            userProfile.setPickup_location(object.getString("pickup_location"));
                            userProfile.setDrop_location(object.getString("drop_location"));
                            userProfile.setUser_phone(object.getString("contact_no"));
                            userProfile.setStart_location_latitude(object.getDouble("start_location_latitude"));
                            userProfile.setStart_location_longitude(object.getDouble("start_location_longitude"));
                            userProfile.setEnd_location_latitude(object.getDouble("end_location_latitude"));
                            userProfile.setEnd_location_longitude(object.getDouble("end_location_longitude"));
                            userProfile.setLatitude(object.getDouble("latitude"));
                            userProfile.setLongitude(object.getDouble("longitude"));

                            ArrayList<LatLng> directionPositionList = getIntent().getParcelableArrayListExtra("direction_points");
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.LTGRAY);

                            LatLng point_to_check = new LatLng(userProfile.getLatitude(), userProfile.getLongitude());

                               // users_asking_for_taxi_adapter.notifyDataSetChanged();
                                LatLng latLng = new LatLng(userProfile.getLatitude(),userProfile.getLongitude());
                                GoogleDirection.withServerKey(getString(R.string.api_key))
                                        .from(new LatLng(point_to_check.latitude,point_to_check.longitude))
                                        .to(new LatLng(start_marker.getPosition().latitude, start_marker.getPosition().longitude))
                                        .transportMode(TransportMode.DRIVING)
                                        .unit(Unit.METRIC)
                                        .execute(new DirectionCallback() {
                                            @Override
                                            public void onDirectionSuccess(Direction direction, String rawBody) {
                                              if(direction!= null && direction.getRouteList().size() > 0) {
                                                  if (Double.parseDouble(direction.getRouteList().get(0).getLegList().get(0).getDistance().getText().split(" ")[0]) < 5) {
                                                      MarkerOptions markerOptions = new MarkerOptions();
                                                      markerOptions.title("Driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon_3d_map));
                                                      markerOptions.position(latLng);
                                                      Marker m = map_show_riders.addMarker(markerOptions);
                                                      m.setPosition(latLng);
                                                      LatLng user_location_latitude_longitude = new LatLng(start_marker.getPosition().latitude, start_marker.getPosition().longitude);

                                                      IconGenerator iconFactory_right = new IconGenerator(getApplicationContext());
                                                      iconFactory_right.setBackground(null);


                                                      View view_right = View.inflate(getApplicationContext(), R.layout.custom_info_window_layout, null);


                                                      iconFactory_right.setContentView(view_right);

                                                      RelativeLayout ll_marker_right = (RelativeLayout) view_right.findViewById(R.id.custom_info_window_layout);

                                                      ll_marker_right.setPadding(600, 200, 0, 0);

                                                      TextView total_time_text = (TextView) view_right.findViewById(R.id.timing_text);
                                                      TextView address_text = (TextView) view_right.findViewById(R.id.address_text);
                                                      TextView title_text = (TextView) view_right.findViewById(R.id.title_text);

                                                      total_time_text.setText(direction.getRouteList().get(0).getLegList().get(0).getDuration().getText());
                                                      Geocoder geocoder = new Geocoder(getApplicationContext());

                                                      userProfile.setTime_to_reach(direction.getRouteList().get(0).getLegList().get(0).getDuration().getText());

                                                      try {
                                                          List<Address> addresses = geocoder.getFromLocation(m.getPosition().latitude, m.getPosition().longitude, 1);
                                                          address_text.setText(addresses.get(0).getAddressLine(0).substring(0, 40));
                                                          title_text.setText(addresses.get(0).getLocality());
                                                      } catch (Exception e) {
                                                          e.printStackTrace();
                                                      }
                                                      drivers_present_to_provide_rides.add(userProfile);
                                                      user_visible_on_map_ids.add(userProfile.getUser_id());
                                                      map_show_riders.addMarker(markerOptions.position(latLng).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory.fromBitmap(iconFactory_right.makeIcon("current"))));
                                                  }
                                              }
                                            }

                                            @Override
                                            public void onDirectionFailure(Throwable t) {

                                            }
                                        });

                        } else {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
//                if (marker != null) {
//                    LatLng start_location = new LatLng(data_received.getDouble("latitude"), data_received.getDouble("longitude"));
//                    animateCar(start_location);
//                    S2LatLng s2LatLng = S2LatLng.fromDegrees(data_received.getDouble("latitude"), data_received.getDouble("longitude"));
//                    Log.d("S2LatLongCellID", "" + S2CellId.fromLatLng(s2LatLng).toToken().substring(0, 8));
//                    Log.d("UPDATING", "MAP LOCATION");
//                    try {
//                        S2RegionCoverer coverer = new S2RegionCoverer();
//                        coverer.setMinLevel(16);
//                        coverer.setMaxLevel(18);
//                        S1Angle angle = S1Angle.degrees(0.01);
//                        S2Point point = S2LatLng.fromDegrees(start_location.latitude, start_location.longitude).toPoint();
//                        S2Cap cap = S2Cap.fromAxisAngle(point, angle);
//                        S2CellUnion union = coverer.getCovering(cap);
//                        for (int i = 0; i < union.cellIds().size(); i++) {
//                            Log.d("S2LatLongCellID region", union.cellIds().get(i).toToken());
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                            start_location, 18.0f));
//                } else {
//                    LatLng start_location = new LatLng(data_received.getDouble("latitude"), data_received.getDouble("longitude"));
//                    markerOptions = new MarkerOptions();
//                    markerOptions.title("Your start location");
//                    markerOptions.position(start_location);
//                    marker = mMap.addMarker(markerOptions);
//                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon_3d_map));
//                    marker.setPosition(start_location);
//                    Log.d("UPDATING", "MAP LOCATION");
//                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
//                            start_location, 18.0f));
//                }

            }
        };

        Messenger my_messanger = new Messenger(share_activity_hander_by_message);
        intent = new Intent(this, driverSearchService.class);
        intent.putExtra("massenger", my_messanger);
        if (getIntent().getStringExtra("user_id") == null) {
            intent.putExtra("user_id", appConfig.getUserId());
        } else {
            intent.putExtra("user_id", getIntent().getExtras().getString("user_id"));
        }

        intent.putExtra("bearer_token", appConfig.getBearerToken());
        intent.putExtra("end_location_latitude", ""+getIntent().getDoubleExtra("end_place_latitude",0));
        intent.putExtra("end_location_longitude", ""+getIntent().getDoubleExtra("end_place_longitude",0));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("SERVICE", "OREO >= ");
            startForegroundService(intent);
        } else {
            Log.d("SERVICE", " < OREO ");
            startService(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter("UPLOAD_COMPLETE");
        registerReceiver(broadcaseReceiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcaseReceiver);
    }


    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return 3;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return new miniFragment();
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return new primeFragment();
                case 2: // Fragment # 1 - This will show SecondFragment
                    return new xlFragment();
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }

    public static void search_ride(String type)
    {
        Log.d("Type of ride: ",""+type);
    }
}
