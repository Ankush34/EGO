package com.ak.ego.share_vehicle_module;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ak.ego.AppConfig;
import com.ak.ego.AppController;
import com.ak.ego.R;
import com.ak.ego.gps_tracker.GPSTracker;
import com.ak.ego.recyclerViewItemClickListener;
import com.ak.ego.share_vehicle_module.SharingUserModule.sharing_users_adapter;
import com.ak.ego.share_vehicle_module.SharingUserModule.userProfile;
import com.ak.ego.share_vehicle_module.screenCustomViewMobule.ArcLayout;
import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.constant.Unit;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.FirstPartyScopes;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2Cap;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2RegionCoverer;
import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.enums.PNReconnectionPolicy;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.access_manager.PNAccessManagerGrantResult;
import com.pubnub.api.models.consumer.channel_group.PNChannelGroupsAddChannelResult;
import com.pubnub.api.models.consumer.presence.PNHereNowChannelData;
import com.pubnub.api.models.consumer.presence.PNHereNowOccupantData;
import com.pubnub.api.models.consumer.presence.PNHereNowResult;
import com.pubnub.api.models.consumer.pubsub.PNMessageResult;
import com.pubnub.api.models.consumer.pubsub.PNPresenceEventResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;

public class start_share_service_activity extends AppCompatActivity implements OnMapReadyCallback {
    public static PubNub pubnub;
    public Timer mTimer = new Timer();
    public FusedLocationProviderClient fusedLocationProviderClient;
    public Intent intent;
    private LocationServiceBroadcastReceiver broadcastReceiver;
    private LocationServiceBounded serviceBounded;
    private ServiceConnection serviceConnection;
    private boolean service_bounded = false;
    private Messenger boundedServiceMessanger;
    private String TAG = "STARTSHAREACTIVITY";
    private GoogleMap mMap;
    private GPSTracker gpsTracker;
    private LatLng location;
    private MarkerOptions markerOptions =new MarkerOptions();
    private Marker marker;
    private RecyclerView users_asking_for_taxi_recycler;
    private sharing_users_adapter users_asking_for_taxi_adapter;
    private ArrayList<userProfile> users_asking_for_taxi_list = new ArrayList<>();


    private Circle lastUserCircle;
    private long pulseDuration = 3000;
    private ValueAnimator lastPulseAnimator;
    private AppConfig appConfig;
    private ArrayList<String> user_visible_on_map_ids = new ArrayList<>();

    private GoogleMap map_presenting_locations;
    private RecyclerView location_selection_recycler;
    private load_location_to_pickup_adapter routes_adapter;
    private ArrayList<Route> routes_to_the_location_of_user = new ArrayList<>();
    private ImageView back_select_travel_route;
    private Marker driverToUserLocationStartMarker;
    private Marker driverToUserLocationEndMarker;
    private MarkerOptions driverToUserLocationStartMarkerOptions;
    private MarkerOptions driverToUserLocationEndMarkerOptions;
    private LatLngBounds.Builder bounds_builder = new LatLngBounds.Builder();
    private LatLngBounds bounds ;

    private BottomSheetBehavior bottomSheetBehavior ;
    private RelativeLayout bottom_sheet_layout;
    private ArcLayout curve_layout;
    private ImageView back_select_riding_user;

    private RecyclerView riding_users_recycler;
    private users_riding_currently_adapter adapter_current_accepted_rides;
    private ArrayList<userProfile> user_profile_rides_accepted_list = new ArrayList<>();
    private userProfile selected_user_to_confirm_ride;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        broadcastReceiver = new LocationServiceBroadcastReceiver();
        appConfig = new AppConfig(getApplicationContext());
        setContentView(R.layout.final_sharing_activity_layout);
        initPubnub();
        if(appConfig.getProviderRideId().equals("null"))
        {
            new create_provider_ride().execute();
        }
        recyclerViewItemClickListener cancel_listener = (view,position)->{
            Toast.makeText(getApplicationContext(),"you tried cancelling ride",Toast.LENGTH_SHORT).show();
        };

        riding_users_recycler = (RecyclerView)findViewById(R.id.riding_users_recycler);
        adapter_current_accepted_rides = new users_riding_currently_adapter(this,user_profile_rides_accepted_list,cancel_listener);
        riding_users_recycler.setHasFixedSize(true);
        riding_users_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        riding_users_recycler.setAdapter(adapter_current_accepted_rides);
        adapter_current_accepted_rides.notifyDataSetChanged();

        bottom_sheet_layout = (RelativeLayout)findViewById(R.id.view_users_sharing_ride_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_layout);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
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
                        new get_all_accepted_rides().execute();
                }
                return true;
            }
        });

        back_select_riding_user = (ImageView)findViewById(R.id.back_select_riding_user);
        back_select_riding_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        RelativeLayout layout_show_user_track_locations = (RelativeLayout)findViewById(R.id.route_selection_recycler_layout);
        back_select_travel_route = (ImageView)findViewById(R.id.back_select_travel_route);
        back_select_travel_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fadeOut(layout_show_user_track_locations);
            }
        });
        //
        //this code is responsible for loading the locations to the users on the road and way to
        //travel to them
        //
        recyclerViewItemClickListener listener_route_selection = (view, position)->{
            Toast.makeText(getApplicationContext(),"selected route "+position,Toast.LENGTH_SHORT).show();
        };
        location_selection_recycler = (RecyclerView)findViewById(R.id.locations_for_selection_recycler);
        routes_adapter = new load_location_to_pickup_adapter(this,routes_to_the_location_of_user,listener_route_selection,"","");
        location_selection_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        location_selection_recycler.setHasFixedSize(true);
        location_selection_recycler.setAdapter(routes_adapter);
        routes_adapter.notifyDataSetChanged();
        SupportMapFragment mapFragmentUserLocations = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_presenting_locations);
        mapFragmentUserLocations.getMapAsync(onMapReadyCallback1());
        //------------------//

        // this code here is responsible for the view that
        // represents all those users that are asking for your taxi_for_share on the way
        users_asking_for_taxi_recycler = (RecyclerView)findViewById(R.id.users_asking_for_taxi_recycler);
        users_asking_for_taxi_recycler.setHasFixedSize(true);
        users_asking_for_taxi_recycler.setLayoutManager(new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false));
        userProfile user = new userProfile();
        userProfile user1 = new userProfile();
        userProfile user2 = new userProfile();
        recyclerViewItemClickListener listener_track_user = (view, position)->{
            Toast.makeText(getApplicationContext(),"Tracking user "+(position+1),Toast.LENGTH_SHORT).show();
            userProfile user_selected = users_asking_for_taxi_list.get(position);
            fadeIn(layout_show_user_track_locations);
            GoogleDirection.withServerKey(getString(R.string.api_key))
                    .from(new LatLng(location.latitude,location.longitude))
                    .to(new LatLng(user_selected.getStart_location_latitude(), user_selected.getStart_location_longitude()))
                    .transportMode(TransportMode.DRIVING)
                    .alternativeRoute(true)
                    .unit(Unit.METRIC).execute(new DirectionCallback() {
                @Override
                public void onDirectionSuccess(Direction direction, String rawBody) {
                    routes_to_the_location_of_user.clear();
                    routes_adapter.notifyDataSetChanged();

                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    if(driverToUserLocationStartMarker == null)
                    {
                        driverToUserLocationStartMarkerOptions = new MarkerOptions();
                        driverToUserLocationStartMarkerOptions.title("Start Here");
                        driverToUserLocationStartMarkerOptions.position(location);
                        driverToUserLocationStartMarker = map_presenting_locations.addMarker(driverToUserLocationStartMarkerOptions);
                    }
                    else
                    {
                        driverToUserLocationStartMarkerOptions.position(location);
                        driverToUserLocationStartMarker.setPosition(location);
                    }

                    if(driverToUserLocationEndMarker == null)
                    {
                        LatLng end_position = new LatLng(user_selected.getStart_location_latitude(),user_selected.getStart_location_longitude());
                        driverToUserLocationEndMarkerOptions = new MarkerOptions();
                        driverToUserLocationEndMarkerOptions.title("End Here");
                        driverToUserLocationEndMarkerOptions.position(end_position);
                        driverToUserLocationEndMarker = map_presenting_locations.addMarker(driverToUserLocationEndMarkerOptions);
                    }
                    else
                    {
                        LatLng end_position = new LatLng(user_selected.getStart_location_latitude(),user_selected.getStart_location_longitude());
                        driverToUserLocationEndMarkerOptions.position(end_position);
                        driverToUserLocationEndMarker.setPosition(end_position);
                    }
                    bounds_builder.include(driverToUserLocationStartMarker.getPosition());
                    bounds_builder.include(driverToUserLocationEndMarker.getPosition());
                    bounds = bounds_builder.build();
                    map_presenting_locations.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,2));

                    try{

                        List<Address>  start_addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        List<Address> end_addresses = geocoder.getFromLocation(user_selected.getStart_location_latitude(),user_selected.getStart_location_longitude(),1);
                        routes_adapter.start_location = start_addresses.get(0).getFeatureName() +", "+ start_addresses.get(0).getAddressLine(0);
                        routes_adapter.end_location = end_addresses.get(0).getFeatureName() +", "+ end_addresses.get(0).getAddressLine(0);

                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    if(direction.isOK()) {
                        List<Route> routes = direction.getRouteList();
                        for (int i = 0; i < routes.size(); i++) {
                            if (i == 0) {
                                ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.GREEN);
                                map_presenting_locations.addPolyline(polylineOptions);
                            } else if (i == 1) {
                                ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.YELLOW);
                                map_presenting_locations.addPolyline(polylineOptions);

                            } else if (i == 2) {
                                ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                                map_presenting_locations.addPolyline(polylineOptions);

                            } else {
                                ArrayList<LatLng> directionPositionList = routes.get(i).getLegList().get(0).getDirectionPoint();
                                PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.DKGRAY);
                                map_presenting_locations.addPolyline(polylineOptions);
                            }
                            Log.d("Route", routes.get(i).getSummary().toString());
                            routes_to_the_location_of_user.add(routes.get(i));
                            routes_adapter.notifyDataSetChanged();

                        }
                    }
                }

                @Override
                public void onDirectionFailure(Throwable t) {
                    Toast.makeText(getApplicationContext(),"Failure in directions fetch please retry !!",Toast.LENGTH_SHORT).show();
                    // Do something
                }
            });

        };

        recyclerViewItemClickListener confirm_ride_listener = (view, position)->{
            Toast.makeText(getApplicationContext(),"confirming this ride",Toast.LENGTH_SHORT).show();
            selected_user_to_confirm_ride = users_asking_for_taxi_list.get(position);
            new create_seeker_ride().execute();
        };
        users_asking_for_taxi_adapter = new sharing_users_adapter(this, users_asking_for_taxi_list,listener_track_user,confirm_ride_listener);
        users_asking_for_taxi_recycler.setAdapter(users_asking_for_taxi_adapter);
        users_asking_for_taxi_adapter.notifyDataSetChanged();

        // mTimer.schedule(new TimerTask(), 5, 1000);

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
    }

    private void initPubnub() {
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey("sub-c-62645bf8-bdd3-11e8-aca1-d2da87ef2ede");
        pnConfiguration.setPublishKey("pub-c-0d621073-bdf4-433d-8b76-8fcde8a90c63");
        pnConfiguration.setUuid("ankush@amuratech.com");
        pnConfiguration.setSecure(true);
        pnConfiguration.setReconnectionPolicy(PNReconnectionPolicy.LINEAR);
        pubnub = new PubNub(pnConfiguration);
        List<String> groups = pubnub.getSubscribedChannelGroups();
        for(int i = 0; i < groups.size();i++)
        {
            Log.d(TAG,groups.get(i).toString());
        }
        List<String> channels = pubnub.getSubscribedChannels();
        for(int i = 0; i < channels.size();i++)
        {
            Log.d(TAG,channels.get(i).toString());
        }
        pubnub.setPresenceState();
        pubnub.publish()
                .message(Arrays.asList("hello"))
                .channel("channel_1")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        Log.d("PUBNUB",""+status.getUuid());
                        Log.d("PUBNUB",""+status.getStatusCode());
                        Log.d("PUBNUB",""+status.getOrigin());
                        Log.d("PUBNUB",""+status.isError());

                    }
                });
        SubscribeCallback subscribeCallback = new SubscribeCallback() {
            @Override
            public void status(PubNub pubnub, PNStatus status) {
                if (status.getCategory() == PNStatusCategory.PNUnexpectedDisconnectCategory)
                {
                    Log.d(TAG, "Unexpected Disconnect from service");
                }
                else if(status.getCategory() == PNStatusCategory.PNConnectedCategory)
                {
                    Log.d(TAG,"Successfully Connnected to the service");
                }

                switch (status.getCategory())
                {
                    case PNConnectedCategory:
                        break;
                    case PNTimeoutCategory:
                        break;
                    case PNBadRequestCategory:
                        break;
                    case PNMalformedResponseCategory:
                        break;
                    case PNUnexpectedDisconnectCategory:
                        break;
                    case PNAccessDeniedCategory:
                        break;
                }

            }

            @Override
            public void message(PubNub pubnub, PNMessageResult message) {
                Log.d(TAG,message.getPublisher());
                Log.d(TAG,message.toString());
            }

            @Override
            public void presence(PubNub pubnub, PNPresenceEventResult presence) {
                if(presence.getEvent().equals("join"))
                {
                    Log.d(TAG,"Join event took place");
                }
                else if(presence.getEvent().equals("leave"))
                {
                    Log.d(TAG,"leave event took place");
                }
                else if(presence.getEvent().equals("timeout"))
                {
                    Log.d(TAG,"timeout event took place");
                }
                else
                {
                    Log.d(TAG,"no matching event "+presence.getEvent());
                }
            }
        };

        pubnub.addListener(subscribeCallback);

        pubnub.subscribe()
                .channels(Arrays.asList("channel_1")) // subscribe to channels
                .withPresence()
                .execute();

        pubnub.grant()
                .manage(true)
                .read(true)
                .write(true)
                .ttl(0)
                .channels(Arrays.asList("channel_1"))
                .async(new PNCallback<PNAccessManagerGrantResult>() {
                    @Override
                    public void onResponse(PNAccessManagerGrantResult result, PNStatus status) {
                        // PNAccessManagerGrantResult is a parsed and abstracted response from server
                        Log.d(TAG,"Permission Grant Status: "+status.getStatusCode());
                        Log.d(TAG,"Permission: "+status.getOperation());
                        Log.d(TAG,"Permission Grand Error: "+status.isError());
                        if(!status.isError())
                        {
                            Log.d(TAG,"level: "+result.getLevel());
                        }
                    }
                });
        pubnub.addChannelsToChannelGroup()
                .channelGroup("channel_group")
                .channels(Arrays.asList("ch1", "ch2", "ch3"))
                .async(new PNCallback<PNChannelGroupsAddChannelResult>() {
                    @Override
                    public void onResponse(PNChannelGroupsAddChannelResult result, PNStatus status) {
                        if(status.isError())
                        {
                            Log.d(TAG,"error occured in adding channel to group");
                            Log.d(TAG,""+status.getStatusCode());
                            Log.d(TAG,""+status.getErrorData());
                            Log.d(TAG,""+status.getOperation());
                        }
                        else
                        {
                            pubnub.subscribe()
                                    .channels(Arrays.asList("ch1","ch2","ch3")) // subscribe to channels
                                    .withPresence()
                                    .execute();
                            Log.d(TAG,"successfully added channels");
                        }
                    }
                });
        pubnub.hereNow()
                .channels(Arrays.asList("channel_1"))
                .includeUUIDs(true)
                .includeState(true)
                .async(new PNCallback<PNHereNowResult>() {
                    @Override
                    public void onResponse(PNHereNowResult result, PNStatus status) {
                        if (status.isError()) {
                            Log.d(TAG,"Error in hereNow"+status.getCategory());
                            return;
                        }
                        Log.d(TAG,"total channels: "+result.getTotalChannels());
                        Log.d(TAG,"Total Occupancy: "+result.getTotalOccupancy());
                        for (PNHereNowChannelData channelData : result.getChannels().values()) {
                            Log.d(TAG, "---");
                            Log.d(TAG,"channel:" + channelData.getChannelName());
                            Log.d(TAG,"occupancy: " + channelData.getOccupancy());
                            Log.d(TAG,"occupants:");
                            for (PNHereNowOccupantData occupant : channelData.getOccupants()) {
                               Log.d(TAG,"uuid: " + occupant.getUuid() + " state: " + occupant.getState());
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        @SuppressLint("HandlerLeak") Handler share_activity_hander_by_message = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d("HANDLERACTIVITY","i received");
                Bundle data_received = msg.getData();
                JSONArray array = (JSONArray)msg.obj;
                Log.d("JSONOBJECTRESPONSE",array.toString());
                try{
                    for(int i = 0 ; i <array.length();i++)
                    {

                        JSONObject object = array.getJSONObject(i);
                        if(!user_visible_on_map_ids.contains(object.getString("id")))
                        {
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

                            ArrayList<LatLng> directionPositionList = getIntent().getParcelableArrayListExtra("route");
                            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.LTGRAY);

                            LatLng point_to_check = new LatLng(userProfile.getLatitude(),userProfile.getLongitude());
                            if(com.google.maps.android.PolyUtil.isLocationOnPath(point_to_check,polylineOptions.getPoints(),true,500))
                            {
                                users_asking_for_taxi_list.add(userProfile);
                                users_asking_for_taxi_adapter.notifyDataSetChanged();
                                user_visible_on_map_ids.add(object.getString("id"));
                            }
                        }
                        else
                        {
                            continue;
                        }
                    }
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                if(marker != null)
                {
                    LatLng start_location = new LatLng(data_received.getDouble("latitude"),data_received.getDouble("longitude"));
                    animateCar(start_location);
                    S2LatLng s2LatLng = S2LatLng.fromDegrees(data_received.getDouble("latitude"),data_received.getDouble("longitude"));
                    Log.d("S2LatLongCellID",""+S2CellId.fromLatLng(s2LatLng).toToken().substring(0,8));
                    Log.d("UPDATING","MAP LOCATION");
                    try {
                        S2RegionCoverer coverer = new S2RegionCoverer();
                        coverer.setMinLevel(16);
                        coverer.setMaxLevel(18);
                        S1Angle angle = S1Angle.degrees(0.01);
                        S2Point point =  S2LatLng.fromDegrees(start_location.latitude, start_location.longitude).toPoint();
                        S2Cap cap =S2Cap.fromAxisAngle(point,angle);
                        S2CellUnion union = coverer.getCovering(cap);
                        for(int i = 0 ; i <union.cellIds().size();i++)
                        {
                            Log.d("S2LatLongCellID region",union.cellIds().get(i).toToken());
                        }
                        }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            start_location, 18.0f));
                }
                else
                {
                    LatLng start_location = new LatLng(data_received.getDouble("latitude"),data_received.getDouble("longitude"));
                    markerOptions = new MarkerOptions();
                    markerOptions.title("Your start location");
                    markerOptions.position(start_location);
                    marker = mMap.addMarker(markerOptions);
                    marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon_3d_map));
                    marker.setPosition(start_location);
                    Log.d("UPDATING","MAP LOCATION");
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            start_location, 18.0f));
                }

            }
        };

        Messenger my_messanger = new Messenger(share_activity_hander_by_message);
        intent = new Intent(this, LocationService.class);
        intent.putExtra("massenger",my_messanger);
        if(getIntent().getStringExtra("vehicle_id") == null)
        {
            intent.putExtra("vehicle_id",appConfig.getCurrentVehicleIdInService());
        }
        else
        {
            intent.putExtra("vehicle_id",getIntent().getExtras().getString("vehicle_id"));
        }
        if(getIntent().getStringExtra("user_id") == null)
        {
            intent.putExtra("user_id",appConfig.getUserId());
        }
        else
        {
            intent.putExtra("vehicle_id",getIntent().getExtras().getString("user_id"));
        }

        intent.putExtra("bearer_token",appConfig.getBearerToken());
        intent.putExtra("end_location_latitude",getIntent().getStringExtra("end_location_latitude"));
        intent.putExtra("end_location_longitude",getIntent().getStringExtra("end_location_longitude"));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.d("SERVICE","OREO >= ");
            startForegroundService(intent);
        }
        else
        {
            Log.d("SERVICE"," < OREO ");
            startService(intent);
        }
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
//                LocationServiceBounded.LocationServiceBinder binder = (LocationServiceBounded.LocationServiceBinder)service;
//                serviceBounded = binder.getService();
//                service_bounded = true;
//                serviceBounded.getMyServiceDone();

                // if we need to ask for a reply to the service
                // we can create a handler in this activity and then pass it on the message
                // the handler in service extracts that messenger and then send the message
                // this message send by the service will be handled by the message handler
                // inside the handler of this activity

                boundedServiceMessanger = new Messenger(service);
                service_bounded = true;
                @SuppressLint("HandlerLeak") Handler bounded_service_reply_handler = new Handler(){
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Log.d("HANDLER","i received the response binded");
                    }
                };
                Messenger messenger = new Messenger(bounded_service_reply_handler);
                Message message = new Message();
                message.obj = messenger;
                message.arg1 = 1;
                try
                {
                    boundedServiceMessanger.send(message);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                boundedServiceMessanger = null;
                service_bounded = false;
            }
        };
        Intent intent = new Intent(this, LocationServiceBounded.class);
        bindService(intent,serviceConnection,Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {

        super.onResume();
        IntentFilter intentFilter = new IntentFilter("UPLOAD_COMPLETE");
        registerReceiver(broadcastReceiver,intentFilter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
            location = new LatLng(Double.parseDouble(stringLatitude), Double.parseDouble(stringLongitude));
            markerOptions.position(location);
            markerOptions.title("Current Location");
            markerOptions.position(location);
            marker = mMap.addMarker(markerOptions);
            marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.car_icon_3d_map));
            marker.setPosition(location);
            ArrayList<LatLng> directionPositionList = getIntent().getParcelableArrayListExtra("route");
            PolylineOptions polylineOptions = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.LTGRAY);


            MarkerOptions markerOptionsStartRide = new MarkerOptions();
            markerOptionsStartRide.title("Start Location");

            LatLng start_latLng = new LatLng(Double.parseDouble(getIntent().getStringExtra("start_location_latitude")),Double.parseDouble(getIntent().getStringExtra("start_location_longitude")));
            markerOptionsStartRide.position(start_latLng);
            Marker start_location_marker  = mMap.addMarker(markerOptionsStartRide);
            start_location_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.start_location));

            MarkerOptions markerOptionsEndRide = new MarkerOptions();
            markerOptionsEndRide.title("End Location");
            LatLng end_latlng = new LatLng(Double.parseDouble(getIntent().getStringExtra("end_location_latitude")),Double.parseDouble(getIntent().getStringExtra("end_location_longitude")));
            markerOptionsEndRide.position(end_latlng);
            Marker end_location_marker = mMap.addMarker(markerOptionsEndRide);
            end_location_marker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.end_location));

            mMap.addPolyline(polylineOptions);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,18.0f));

        }
        else if(gpsTracker!= null)
        {
            gpsTracker.showSettingsAlert();
        }
        // Add a marker in Sydney, Australia, and move the camera.
    }

    private void animateCar(final LatLng destination) {
        final LatLng startPosition = marker.getPosition();
        final LatLng endPosition = new LatLng(destination.latitude, destination.longitude);
        final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(5000); // duration 5 seconds
        valueAnimator.setInterpolator(new LinearInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    marker.setPosition(newPosition);

                } catch (Exception ex) {
                }
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                CircleOptions circleOptions = new CircleOptions();
                circleOptions.center(destination);
                circleOptions.radius(25);
                circleOptions.strokeColor(Color.GRAY);
                if(lastUserCircle  == null)
                {
                    lastUserCircle = mMap.addCircle(circleOptions);
                }

                addPulsatingEffect(destination);

            }
        });
        valueAnimator.start();
    }

    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);
        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
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

    public class TimerTask extends java.util.TimerTask
    {

        @Override
        public void run() {
            pubnub.publish()
                    .message(Arrays.asList("hello", "i","am","aseem"))
                    .channel("channel_1")
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            Log.d("PUBNUB",""+status.getUuid());
                            Log.d("PUBNUB",""+status.getStatusCode());
                            Log.d("PUBNUB",""+status.getOrigin());
                            Log.d("PUBNUB",""+status.isError());

                        }
                    });
        }
    }

    private void addPulsatingEffect(LatLng userLatlng){
        if(lastPulseAnimator != null){
            lastPulseAnimator.cancel();
            Log.d("onLocationUpdated: ","cancelled" );
        }
        if(lastUserCircle != null)
            lastUserCircle.setCenter(userLatlng);
        lastPulseAnimator = valueAnimate(getDisplayPulseRadius(25.0f), pulseDuration, new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(lastUserCircle != null)
                {
                    lastUserCircle.setRadius(getDisplayPulseRadius((Float) animation.getAnimatedValue()));
                    lastUserCircle.setFillColor(adjustAlpha(Color.alpha(Color.GRAY), 1 - animation.getAnimatedFraction()));
                }
                else {
                    lastUserCircle = mMap.addCircle(new CircleOptions()
                            .center(userLatlng)
                            .radius((Float) animation.getAnimatedValue())
                            .strokeColor(Color.RED)
                            .fillColor(Color.BLUE));
                }
            }
        });

    }


    protected float getDisplayPulseRadius(float radius) {
        float diff = (mMap.getMaxZoomLevel() - mMap.getCameraPosition().zoom);
            return radius;
         }

    protected ValueAnimator valueAnimate(float accuracy,long duration, ValueAnimator.AnimatorUpdateListener updateListener){
        Log.d( "valueAnimate: ", "called");
        ValueAnimator va = ValueAnimator.ofFloat(0,accuracy);
        va.setDuration(duration);
        va.addUpdateListener(updateListener);
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.RESTART);
        va.start();
        return va;
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }


    public OnMapReadyCallback onMapReadyCallback1(){
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map_presenting_locations = googleMap;
                if (gpsTracker!= null && gpsTracker.getIsGPSTrackingEnabled())
                {
                    String stringLatitude = String.valueOf(gpsTracker.latitude);
                    Log.d("Latitiude",stringLatitude);
                    String stringLongitude = String.valueOf(gpsTracker.longitude);
                    Log.d("Latitiude",stringLongitude);
                    LatLng location = new LatLng(Double.parseDouble(stringLatitude), Double.parseDouble(stringLongitude));
                    driverToUserLocationStartMarkerOptions = new MarkerOptions();
                    driverToUserLocationStartMarkerOptions.title("Start Here");
                    driverToUserLocationStartMarkerOptions.position(location);
                    driverToUserLocationStartMarker = map_presenting_locations.addMarker(driverToUserLocationStartMarkerOptions);
                    map_presenting_locations.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18.0f));

                }
                else if(gpsTracker!= null)
                {
                    gpsTracker.showSettingsAlert();
                }
            }
        };
    }


    public void fadeOut(View v)
    {

        Animation fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        fadeOutAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) { v.setVisibility(View.INVISIBLE); }
            @Override public void onAnimationRepeat(Animation animation) { }
        });
        v.startAnimation(fadeOutAnimation);
    }

    public void fadeIn(View v)
    {

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override public void onAnimationStart(Animation animation) { }
            @Override public void onAnimationEnd(Animation animation) { v.setVisibility(View.VISIBLE);}
            @Override public void onAnimationRepeat(Animation animation) { }
        });
        v.startAnimation(fadeInAnimation);
    }

    public class get_all_accepted_rides extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            JsonArrayRequest request_seeker_rides = new JsonArrayRequest(appConfig.get_seeker_rides_url+appConfig.getProviderRideId(), new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    Log.d("SEEKERRIDES",response.toString());
                    try
                    {
                        user_profile_rides_accepted_list.clear();
                        adapter_current_accepted_rides.notifyDataSetChanged();

                        for(int i = 0; i < response.length(); i++)
                        {
                            JSONObject object = response.getJSONObject(i);
                            userProfile profile = new userProfile();
                            profile.setStart_location_longitude(Double.parseDouble(object.getString("seeker_start_location_longitude")));
                            profile.setStart_location_latitude(Double.parseDouble(object.getString("seeker_start_location_latitude")));
                            profile.setEnd_location_latitude(Double.parseDouble(object.getString("seeker_end_location_latitude")));
                            profile.setEnd_location_longitude(Double.parseDouble(object.getString("seeker_end_location_longitude")));
                            profile.setSeeker_ride_id(object.getString("id"));
                            profile.setProvider_ride_id(object.getString("provider_ride_id"));
                            profile.setProvider_ride_user_id(object.getString("provider_id"));
                            profile.setName(object.getString("user_name"));
                            profile.setUser_phone(object.getString("user_phone"));
                            profile.setRide_cost(object.getString("total_ride_cost"));
                            user_profile_rides_accepted_list.add(profile);
                            adapter_current_accepted_rides.notifyDataSetChanged();
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("VOLLEYERROR","SEEKERRIDES"+error.toString());
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
            request_seeker_rides.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(request_seeker_rides);

            return null;
        }
    }

    public class create_provider_ride extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject params = new JSONObject();
            JSONObject content = new JSONObject();
            try{
                content.put("provider_start_location_latitude",getIntent().getStringExtra("start_location_latitude"));
                content.put("provider_start_location_longitude",getIntent().getStringExtra("start_location_longitude"));
                content.put("provider_end_location_latitude",getIntent().getStringExtra("end_location_latitude"));
                content.put("provider_end_location_longitude",getIntent().getStringExtra("end_location_longitude"));
                params.put("provider_ride",content);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            JsonObjectRequest request_create_new_validtor_ride = new JsonObjectRequest(Request.Method.POST, appConfig.create_provider_ride_url, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("RESPONSECREATEPROVIDERRIDE",response.toString());
                    try
                    {
                        JSONObject response_object = response.getJSONObject("provider_ride");
                        appConfig.setProviderRideId(response_object.getString("id"));
                        Log.d("RESPONSECREATEPROVIDERRIDE",appConfig.getProviderRideId());
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("RESPONSECREATEPROVIDERRIDE","Error: "+error.toString());
                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json; charset=UTF-8");
                    params.put("Authorization","Bearer " + appConfig.getBearerToken());
                    return params;
                }

                @Override
                public String getBodyContentType()
                {
                    return "application/json";
                }
            };
            request_create_new_validtor_ride.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(request_create_new_validtor_ride);
            return null;
        }
    }

    private class create_seeker_ride extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject params = new JSONObject();
            JSONObject content = new JSONObject();
            try
            {
                params.put("user_id",selected_user_to_confirm_ride.getUser_id());
                params.put("provider_ride_id",appConfig.getProviderRideId());
                content.put("seeker_start_location_latitude",selected_user_to_confirm_ride.getStart_location_latitude());
                content.put("seeker_start_location_longitude",selected_user_to_confirm_ride.getStart_location_longitude());
                content.put("seeker_end_location_latitude",selected_user_to_confirm_ride.getEnd_location_latitude());
                content.put("seeker_end_location_longitude",selected_user_to_confirm_ride.getEnd_location_longitude());
                content.put("total_ride_cost","1800");
                params.put("seeker_ride",content);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            JsonObjectRequest request_create_seeker_ride = new JsonObjectRequest(Request.Method.POST, appConfig.create_seeker_ride_url, params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if(response.has("provider_ride"))
                        {
                            JSONObject provider_ride_containing_this_seeker_ride = response.getJSONObject("provider_ride");
                            JSONArray seeker_rides_array = provider_ride_containing_this_seeker_ride.getJSONArray("seeker_rides");
                            users_asking_for_taxi_list.remove(selected_user_to_confirm_ride);
                            users_asking_for_taxi_adapter.notifyDataSetChanged();
                        }
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(),"Could Not confirm ride please retry! ",Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json; charset=UTF-8");
                    params.put("Authorization","Bearer " + appConfig.getBearerToken());
                    return params;
                }

                @Override
                public String getBodyContentType()
                {
                    return "application/json";
                }
            };
            request_create_seeker_ride.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(request_create_seeker_ride);
            return null;
        }
    }
}
