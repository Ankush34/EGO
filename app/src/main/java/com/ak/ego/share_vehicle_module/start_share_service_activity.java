package com.ak.ego.share_vehicle_module;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.ak.ego.R;
import com.ak.ego.gps_tracker.GPSTracker;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        broadcastReceiver = new LocationServiceBroadcastReceiver();
        setContentView(R.layout.final_sharing_activity_layout);
        initPubnub();

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
        Handler share_activity_hander_by_message = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                Log.d("HANDLERACTIVITY","i received");
                Bundle data_received = msg.getData();
                if(marker != null)
                {
                    LatLng start_location = new LatLng(data_received.getDouble("latitude"),data_received.getDouble("longitude"));
                    animateCar(start_location);
                    Log.d("UPDATING","MAP LOCATION");
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
                Handler bounded_service_reply_handler = new Handler(){
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
            marker.setPosition(location);
            marker.setIcon(BitmapDescriptorFactory.defaultMarker());
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
}
