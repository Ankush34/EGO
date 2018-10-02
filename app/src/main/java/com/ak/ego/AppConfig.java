package com.ak.ego;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfig {
    public static String url_base = "http://6523f000.ngrok.io";
    public static String signup_url = url_base+"/users/create_user_from_api";
    public static String login_url = url_base+"/users/sign_in.json";
    public static String get_all_users = url_base+"/users.json";
    public static String get_all_users_vehicles = url_base+"/vehicles.json?email=";

    // we send a patch request to this route which goes to update action of the vehicle controller
    public static String update_user_location = url_base+"/users/update_user_from_api";
    public static String bearer_token = "";
    public static String get_seeker_rides_url = url_base+"/seeker_rides.json?provider_ride_id=";
    public static String create_seeker_ride_url = url_base+"/seeker_rides.json";
    public static String create_provider_ride_url = url_base+"/provider_rides"+".json";
    public static String get_provider_rides = url_base+"/provider_rides"+".json";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static String PREF_NAME = "Khurana_sales_pref";

    public AppConfig()
    {

    }


    public AppConfig(Context context)
    {
        int private_mode = 0;
        pref = context.getSharedPreferences(PREF_NAME,private_mode);
        editor = pref.edit();
        editor.commit();
    }

    public void setProviderRideId(String provider_ride_id)
    {
        editor.putString("provider_ride_id",provider_ride_id);
        editor.commit();
    }

    public String getProviderRideId(){
        return pref.getString("provider_ride_id","null");
    }

    public void set_user_name(String user_name)
    {
        editor.putString("user_name",user_name);
        editor.commit();
    }

    public void setUserId(String id)
    {
        editor.putString("user_id",id);
        editor.commit();
    }

    public String getUserId(){
        return pref.getString("user_id","");
    }

    public void set_current_vehicle_id_in_service(String vehicle_id)
    {
        editor.putString("vehicle_id",vehicle_id);
        editor.commit();
    }

    public String getCurrentVehicleIdInService()
    {
        return pref.getString("vehicle_id","");
    }


    public String getUserName()
    {
        return pref.getString("user_name","Ankush Khurana");
    }


    public void set_bearer_token(String bearer_token)
    {
        editor.putString("bearer_token",bearer_token);
        editor.commit();
    }
    public String getBearerToken()
    {
        return pref.getString("bearer_token","----");
    }

    public void setStatus_login(Boolean status_login)
    {
        editor.putBoolean("status_login",status_login);
        editor.commit();
    }

    public Boolean isLogin()
    {
        return  pref.getBoolean("status_login",false);
    }

    public void setUser_name(String user_name) {
        editor.putString("user_name",user_name);
        editor.commit();
    }

    public String getUser_name() {
        return pref.getString("user_name","Ankush Khurana");
    }

    public void setUser_email(String user_email)
    {
        editor.putString("user_email",user_email);
        editor.commit();
    }

    public String getUser_email() { return pref.getString("user_email","ankushkhurana34@gmail.com"); }


}
