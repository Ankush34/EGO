package com.ak.ego.share_vehicle_module;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

import static com.ak.ego.share_vehicle_module.MyIntentBuilder.containsCommand;

public class LocationService extends Service implements LocationListener {

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    double latitude,longitude;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();
    private Timer mTimer = null;
    long notify_interval = 5000;
    public static String str_receiver = "location_service";
    Intent intent;
    private volatile Looper mLooper;
    private volatile LocationServiceHandler serviceHandler;
    private int start_id;
    private Intent intent_sent_by_activity;
    private Boolean mServiceIsStarted = false;

    public LocationService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        start_id = startId;
        intent_sent_by_activity = intent;
        HandlerThread handlerThread = new HandlerThread("UploadDataServiceHandler");
        handlerThread.start();
        mLooper = handlerThread.getLooper();
        serviceHandler = new LocationServiceHandler(mLooper);
        routeIntentToCommand(intent_sent_by_activity);
        return START_REDELIVER_INTENT;
    }

    private void routeIntentToCommand(Intent intent) {
        if (intent != null) {

            // process command
            if (containsCommand(intent)) {
                processCommand(MyIntentBuilder.getCommand(intent));
            }

            // process message
            if (MyIntentBuilder.containsMessage(intent)) {
                processMessage(MyIntentBuilder.getMessage(intent));
            }
        }
    }

    private void processMessage(String message) {
        try {
            Log.d("LOCATIONSERVICE", String.format("doMessage: message from client: '%s'", message));

        } catch (Exception e) {
            Log.e("LOCATIONSERVICE", "processMessage: exception", e);
        }
    }

    private void processCommand(int command) {
        try {
            switch (command) {
                case Command.START:
                    commandStart();
                    break;
                case Command.STOP:
                    commandStop();
                    break;
            }
        } catch (Exception e) {
            Log.e("LOCATIONSERVICE", "processCommand: exception");
        }
    }


    private void commandStop() {
        stopForeground(true);
        stopSelf();
    }

    private void moveToStartedState() {

        Intent intent = new MyIntentBuilder(this).setCommand(Command.START).build();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Log.d("SERVICE","BEFORE OREO");
            startService(intent);
        } else {
            Log.d("SERVICE", "moveToStartedState: Running on Android O - startForegroundService(intent)");
            startForegroundService(intent);
        }
    }

    private void commandStart() {

        if (!mServiceIsStarted) {
            moveToStartedState();
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                HandleNotifications.PreO.createNotification(this);
            } else {
                HandleNotifications.O.createNotification(this);
            }
        }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.d("SERVICE","OREO >= ");
            HandleNotifications.O.createNotification(this);
        }
        else
        {
            Log.d("SERVICE"," < OREO ");
            HandleNotifications.PreO.createNotification(this);
        }
        mTimer = new Timer();
        mTimer.schedule(new TimerTaskToGetLocation(),5,notify_interval);
        intent = new Intent(str_receiver);
//        fn_getlocation();
    }

    @Override
    public void onLocationChanged(Location location) {
            Log.e("latitude-longitude", "onLocationChanged: " + location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e("latitude-longitude", "onStatusChanged: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e("latitude-longitude", "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e("latitude-longitude", "onProviderDisabled: " + provider);
    }

    @SuppressLint("MissingPermission")
    private void fn_getlocation(){
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (!isGPSEnable && !isNetworkEnable){

        }else {

            if (isNetworkEnable){
                location = null;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location!=null){

                        Log.e("latitude",location.getLatitude()+"");
                        Log.e("longitude",location.getLongitude()+"");

                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                        Intent replyIntent = new Intent("UPLOAD_COMPLETE");
                        // for setting the broadcast only for this app
                        // replyIntent.setPackage();
                        sendBroadcast(replyIntent);
                    }
                }

            }


            if (isGPSEnable){
                location = null;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,0,this);
                if (locationManager!=null){
                    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (location!=null){
                        Log.e("latitude",location.getLatitude()+"");
                        Log.e("longitude",location.getLongitude()+"");
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                        fn_update(location);
                        Intent replyIntent = new Intent("UPLOAD_COMPLETE");
                        // for setting the broadcast only for this app
                        // replyIntent.setPackage();
                        sendBroadcast(replyIntent);
                    }
                }
            }


        }
        Message message = serviceHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putDouble("latitude",latitude);
        bundle.putDouble("longitude",longitude);
        message.setData(bundle);
        message.obj = intent_sent_by_activity;
        serviceHandler.sendMessage(message);

    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    fn_getlocation();
                }
            });

        }
    }

    private void fn_update(Location location){

        intent.putExtra("latitude",location.getLatitude()+"");
        intent.putExtra("longitude",location.getLongitude()+"");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLooper.quit();
    }


}
