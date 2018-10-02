package com.ak.ego.bookShareCarRideModule;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.ak.ego.AppConfig;
import com.ak.ego.AppController;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.common.geometry.S2CellId;
import com.google.common.geometry.S2LatLng;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.ak.ego.share_vehicle_module.MyIntentBuilder.containsCommand;

public class driverSearchService extends Service implements LocationListener {

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
    private volatile driverSearchHandler serviceHandler;
    private int start_id;
    private Intent intent_sent_by_activity;
    private Boolean mServiceIsStarted = false;

    public driverSearchService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("DRIVERSEARCHSERVICE","in on start_command");
        start_id = startId;
        intent_sent_by_activity = intent;
        HandlerThread handlerThread = new HandlerThread("UploadDataServiceHandler");
        handlerThread.start();
        mLooper = handlerThread.getLooper();
        serviceHandler = new driverSearchHandler(mLooper);
        routeIntentToCommand(intent_sent_by_activity);
        return START_REDELIVER_INTENT;
    }

    private void routeIntentToCommand(Intent intent) {
        if (intent != null) {

            // process command
            if (containsCommand(intent)) {
                processCommand(driverSearchIntentBuilder.getCommand(intent));
            }

            // process message
            if (driverSearchIntentBuilder.containsMessage(intent)) {
                processMessage(driverSearchIntentBuilder.getMessage(intent));
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
        mTimer.cancel();
        mLooper.quit();
        locationManager.removeUpdates(this);

    }

    private void moveToStartedState() {

        Intent intent = new driverSearchIntentBuilder(this).setCommand(Command.START).build();
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
            HandleNotificationsDriverSearch.PreO.createNotification(this);
        } else {
            HandleNotificationsDriverSearch.O.createNotification(this);
        }
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("DRIVERSEARCHSERVICE","in on create");
        super.onCreate();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.d("SERVICE","OREO >= ");
            HandleNotificationsDriverSearch.O.createNotification(this);
        }
        else
        {
            Log.d("SERVICE"," < OREO ");
            HandleNotificationsDriverSearch.PreO.createNotification(this);
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
        bundle.putDouble("end_latitude",Double.parseDouble(intent_sent_by_activity.getStringExtra("end_location_latitude")));
        bundle.putDouble("end_longitude",Double.parseDouble(intent_sent_by_activity.getStringExtra("end_location_longitude")));
        bundle.putString("bearer_token",intent_sent_by_activity.getStringExtra("bearer_token"));
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
      //  new upload_start_end_location().execute();
        Double[] data = new Double[2];
        data[0] = latitude;
        data[1] = longitude;
//        new snap_to_road().execute(data);
        sendBroadcast(intent);
    }

    public class upload_start_end_location extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject params= new JSONObject();
            JSONObject parent = new JSONObject();
            JSONObject top = new JSONObject();
            try {
                params.put("latitude",location.getLatitude());
                params.put("longitude",location.getLongitude());
                top.put("location_attributes",params);
                top.put("service_type","PROVIDER");
                S2LatLng s2LatLng = S2LatLng.fromDegrees(location.getLatitude(),location.getLongitude());
                params.put("s2_region_id",S2CellId.fromLatLng(s2LatLng).toToken().substring(0,8));
                parent.put("user",top);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT,AppConfig.update_user_location+".json",parent, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("UPDATESTARTLOCATION: ",""+response.toString());
                    try {
                        Log.d("Writing Location", "Successfully written your route");
                    }catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("VolleyError",""+error.getMessage());
                    Toast.makeText(getApplicationContext(),"Error Took Place , Please Check Your Network Connection",Toast.LENGTH_SHORT).show();
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json; charset=UTF-8");
                    params.put("Authorization","Bearer " + intent_sent_by_activity.getStringExtra("bearer_token"));
                    return params;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json";
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(request);
            return null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ONDESTROY","i am destroyed");
        mLooper.quit();
    }

    public class snap_to_road extends AsyncTask<Double, Void, Void>{
        @Override
        protected Void doInBackground(Double... data) {
            JSONObject params = new JSONObject();
            if(location != null)
            {
                JsonObjectRequest request_snap_to_road = new JsonObjectRequest(Request.Method.GET, "https://roads.googleapis.com/v1/snapToRoads?path="+data[0]+","+data[1]+"&interpolate=true&key=AIzaSyBdxCv6jM9VmZ5XP1W5fEVa6he9Kx3Xg5E", params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.length() > 0)
                            {
                                JSONArray array_response = response.getJSONArray("snappedPoints");
                                if(array_response.length() > 0)
                                {
                                    JSONObject snapped_points_json = array_response.getJSONObject(0);
                                    JSONObject snapped_points_location = snapped_points_json.getJSONObject("location");
                                    Double latitude = snapped_points_location.getDouble("latitude");
                                    Double longitude = snapped_points_location.getDouble("longitude");
                                    Log.d("Snapped latitude",""+latitude);
                                    Log.d("Snapped longitude",""+longitude);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("SNAP","Sorry could not snap to road");
                    }
                });
                request_snap_to_road.setRetryPolicy(new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                AppController.getInstance().addToRequestQueue(request_snap_to_road);
            }
            return  null;
        }
    }

}
