package com.ak.ego.signUpActivity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.ak.ego.R;

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
        user_email = (EditText)findViewById(R.id.user_email_text);
        user_password = (EditText)findViewById(R.id.user_password_text);
        user_password_confirm = (EditText)findViewById(R.id.confirm_password);
        user_phone = (EditText)findViewById(R.id.user_phone);
        signup_button = (ImageView)findViewById(R.id.signup_button);

        signup_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    public class signup extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
