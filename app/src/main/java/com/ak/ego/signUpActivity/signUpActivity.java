package com.ak.ego.signUpActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.ak.ego.AppConfig;
import com.ak.ego.AppController;
import com.ak.ego.R;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class signUpActivity extends Activity {
    public EditText user_email;
    public EditText user_password;
    public EditText user_password_confirm;
    public EditText user_phone;
    public ImageView signup_button;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup_activity_layout);
        user_email = (EditText)findViewById(R.id.email);
        user_password = (EditText)findViewById(R.id.password);
        user_password_confirm = (EditText)findViewById(R.id.confirm_password);
        user_phone = (EditText)findViewById(R.id.user_phone);
        signup_button = (ImageView)findViewById(R.id.signup_button);

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new signup().execute();
            }
        });

    }

    public class signup extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject params = new JSONObject();
            JSONObject params_main  = new JSONObject();
            try
            {
              params.put("name","ankush khurana");
              params.put("email",user_email.getText().toString().trim());
              params.put("password",user_password.getText().toString().trim());
              params.put("contact_no",user_phone.getText().toString().trim());
              params_main.put("user", params);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,AppConfig.signup_url,params_main, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("SignUpActivity: ",""+response.toString());
                    try {
                        if(response.getString("status").equals("Successfully Registered"))
                        {
                            Toast.makeText(getApplicationContext(),"Successfully done signup",Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Please Retry !! ",Toast.LENGTH_LONG).show();

                        }
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
            });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            AppController.getInstance().addToRequestQueue(request);
            return null;
        }
    }
}
