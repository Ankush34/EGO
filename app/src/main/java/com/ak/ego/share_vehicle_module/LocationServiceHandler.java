package com.ak.ego.share_vehicle_module;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.ak.ego.AppConfig;
import com.ak.ego.AppController;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.common.geometry.S1Angle;
import com.google.common.geometry.S2Cap;
import com.google.common.geometry.S2CellUnion;
import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2LatLngRect;
import com.google.common.geometry.S2Point;
import com.google.common.geometry.S2RegionCoverer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.chrono.MinguoEra;
import java.util.HashMap;
import java.util.Map;

public class LocationServiceHandler extends Handler {

    public LocationServiceHandler(Looper looper)
    {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Log.d("HANDLER",""+msg.arg1);
        Bundle data_received = msg.getData();
        Intent received_intent = (Intent)msg.obj;

        Double start_latitude = data_received.getDouble("latitude");
        Double end_latitude = (Double)data_received.getDouble("end_latitude");
        Double start_longitude = (Double)data_received.getDouble("longitude");
        Double end_longitude = (Double)data_received.getDouble("end_longitude");
        String bearer_token  = (String)data_received.getString("bearer_token");
        S2LatLng s2LatLng1 = S2LatLng.fromDegrees(start_latitude,start_longitude);
        S2LatLng s2LatLng2 = S2LatLng.fromDegrees(end_latitude, end_longitude);

            S2RegionCoverer coverer_circular_region = new S2RegionCoverer();
            coverer_circular_region.setMinLevel(16);
            coverer_circular_region.setMaxLevel(18);
            S1Angle angle = S1Angle.degrees(0.01);
            S2Point point =  S2LatLng.fromDegrees(start_latitude, start_longitude).toPoint();
            S2Cap cap = S2Cap.fromAxisAngle(point,angle);
            S2CellUnion union = coverer_circular_region.getCovering(cap);
            for(int i = 0 ; i <union.cellIds().size();i++)
            {
                Log.d("HANDLERCELLIDS",union.cellIds().get(i).toToken());
            }

        S2LatLngRect latLngRect = S2LatLngRect.fromPointPair(s2LatLng1,s2LatLng2);
        S2RegionCoverer coverer = new S2RegionCoverer();
        coverer.setMinLevel(16);
        coverer.setMaxLevel(18);
        S2CellUnion cellUnion = coverer.getCovering(latLngRect);
        StringBuilder tokens = new StringBuilder("");
        Messenger messenger = (Messenger)received_intent.getExtras().get("massenger");
        Message message = new Message();
        cellUnion.cellIds().addAll(union.cellIds());

        post(new Runnable() {
            @Override
            public void run() {
                for(int i  = 0 ; i < cellUnion.cellIds().size();i++)
                {
                    Log.d("HANDLERCELLIDS",cellUnion.cellIds().get(i).toToken());
                    if(cellUnion.cellIds().get(i).toToken().length() < 8)
                    {
                        tokens.append(cellUnion.cellIds().get(i).toToken()+" ");
                    }
                    else
                    {
                        tokens.append(cellUnion.cellIds().get(i).toToken().substring(0,8)+" ");
                    }
                }
                JSONObject params = new JSONObject();
                JsonObjectRequest request_users_in_these_regions = new JsonObjectRequest(Request.Method.GET,AppConfig.get_all_users,params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("RESPONSEHANDLER",response.toString());
                        try {
                            JSONArray response_users_requesting_ride_on_way =  response.getJSONArray("total_users");
                            if(response_users_requesting_ride_on_way.length() > 0)
                            {
                             message.obj = response_users_requesting_ride_on_way;
                             message.setData(data_received);
                             try {
                                 messenger.send(message);
                             }
                             catch (Exception e)
                             {
                                 e.printStackTrace();
                             }

                            }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("RESPONSEHANDLER","ERROR"+error.toString());
                    }
                })
                    {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Content-Type", "application/json; charset=UTF-8");
                        params.put("Authorization","Bearer " + bearer_token);
                        params.put("fltrs",tokens.toString().trim());
                        return params;
                    }

                        @Override
                        public String getBodyContentType() {
                        return "application/json";
                    }
                    };

                AppController.getInstance().addToRequestQueue(request_users_in_these_regions);
                request_users_in_these_regions.setRetryPolicy(new DefaultRetryPolicy(
                        DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            }
        });
    }
}
