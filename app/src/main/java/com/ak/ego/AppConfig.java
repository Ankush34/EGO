package com.ak.ego;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfig {
    public static String url_base = "http://03de6933.ngrok.io";
    public static String signup_url = url_base+"/users/create_user_from_api";
    public static String login_url = url_base+"/users/sign_in.json";
    public static String get_all_users = url_base+"/users.json";
    public static String get_all_users_vehicles = url_base+"/vehicles.json?email=";
    public static String bearer_token = "";
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

    public void set_user_name(String user_name)
    {
        editor.putString("user_name",user_name);
        editor.commit();
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
