package com.ak.ego.loginActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ak.ego.AppConfig;
import com.ak.ego.AppController;
import com.ak.ego.R;
import com.ak.ego.mainActivityModule.mainActivity;
import com.ak.ego.signUpActivity.signUpActivity;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class loginActivityMain extends Activity {
    public ImageView login_by_google;
    public TextView not_a_user;
    public String id;
    public EditText email;
    public ImageView login_button_direct;
    public EditText password;
    public com.facebook.login.widget.LoginButton login_by_facebook_button;
    public ImageView login_by_facebook_custom_button;
    public int RC_SIGN_IN = 100;
    private static final String EMAIL = "email";
    public CallbackManager callbackManager;
    public AppConfig appConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);

        appConfig = new AppConfig(getApplicationContext());
        if(appConfig.isLogin())
        {
            Toast.makeText(getApplicationContext(),"Your Login Is Active",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, mainActivity.class);
            startActivity(intent);
            finish();
        }
        login_button_direct = (ImageView)findViewById(R.id.login_button_direct);
        login_button_direct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               new try_login().execute();
            }
        });
        email = (EditText)findViewById(R.id.user_email_text);
        password = (EditText)findViewById(R.id.user_password_text);
        
        not_a_user = (TextView) findViewById(R.id.not_a_user);
        not_a_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), signUpActivity.class);
                    startActivity(intent);
                finish();
            }
        });
        login_by_facebook_custom_button = (ImageView) findViewById(R.id.login_by_facebook);
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.ak.ego",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            Toast.makeText(getApplicationContext(), "Ur facebook login is still active", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, mainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(getApplicationContext(), "Ur facebook login has expired", Toast.LENGTH_SHORT).show();
        }
        login_by_google = (ImageView) findViewById(R.id.login_by_google);
        login_by_facebook_custom_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login_by_facebook_button.performClick();
            }
        });
        login_by_facebook_button = (com.facebook.login.widget.LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();
        login_by_facebook_button.setReadPermissions(Arrays.asList(EMAIL));
        login_by_facebook_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Profile profile = Profile.getCurrentProfile().getCurrentProfile();
                if (profile != null) {
                    Toast.makeText(getApplicationContext(), "You Are Logged In From Facebook / Now Logging Out", Toast.LENGTH_SHORT).show();
                    new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/permissions/", null, HttpMethod.DELETE, new GraphRequest
                            .Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {

                            LoginManager.getInstance().logOut();

                        }
                    }).executeAsync();
                } else {
                    Toast.makeText(getApplicationContext(), "Logging in..", Toast.LENGTH_SHORT).show();
                    LoginManager.getInstance().logInWithReadPermissions(loginActivityMain.this, Arrays.asList("public_profile", "email",
                            "user_birthday"));
                }

            }
        });


        login_by_facebook_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Logged In By Facebook", Toast.LENGTH_SHORT).show();
                System.out.println("onSuccess");
                Intent intent = new Intent(loginActivityMain.this, mainActivity.class);
                startActivity(intent);
                String accessToken = loginResult.getAccessToken()
                        .getToken();
                Log.i("accessToken", accessToken);

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object,
                                                    GraphResponse response) {
                                Log.i("LoginActivity", response.toString());
                                try {
                                    id = object.getString("id");
                                    try {
                                        URL profile_pic = new URL(
                                                "http://graph.facebook.com/" + id + "/picture?type=large");
                                        Log.i("profile_pic",
                                                profile_pic + "");

                                    } catch (MalformedURLException e) {
                                        e.printStackTrace();
                                    }
                                    String name = object.getString("name");
                                    String email = object.getString("email");
                                    String gender = object.getString("gender");
                                    String birthday = object.getString("birthday");
                                    appConfig.setStatus_login(true);
                                    appConfig.setUser_email(email);
                                    appConfig.setUser_name(name);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putString("fields",
                        "id,name,email,gender, birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                appConfig.setStatus_login(false);
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(), "There was an error in signin", Toast.LENGTH_SHORT).show();
                appConfig.setStatus_login(false);
            }
        });
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        final GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        login_by_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(loginActivityMain.this);
                if (account == null) {
                    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                    appConfig.setStatus_login(false);
                } else {
                    appConfig.setStatus_login(true);
                    appConfig.setUser_email(account.getEmail());
                    appConfig.setUser_name(account.getDisplayName());
                    Toast.makeText(getApplicationContext(), "U r signed in previously in app", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(loginActivityMain.this, mainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            appConfig.setStatus_login(true);
            appConfig.setUser_email(account.getEmail());
            appConfig.setUser_name(account.getDisplayName());
            // Signed in successfully, show authenticated UI.
            Toast.makeText(getApplicationContext(), "You have signed in successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(loginActivityMain.this, mainActivity.class);
            startActivity(intent);
            finish();
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            appConfig.setStatus_login(false);
            Log.w("GOOGLE SIGN IN", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(getApplicationContext(), "Sorry Error Took Place", Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public class try_login extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            JSONObject params = new JSONObject();
            JSONObject params_final = new JSONObject();
             try {
                params.put("email",email.getText().toString().toLowerCase().trim());
                params.put("password",password.getText().toString().trim());
                params_final.put("user", params);
             }catch (Exception e)
             {
                 e.printStackTrace();
             }
            JsonObjectRequest login_request = new JsonObjectRequest(Request.Method.POST, AppConfig.login_url, params_final,
                    new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("Response",response.toString());
                    try {
                           if(response.getString("success").equals("true"))
                           {
                             Toast.makeText(getApplicationContext(),"Successfully Logged In !!",Toast.LENGTH_SHORT).show();
                             JSONObject user = response.getJSONObject("user");
                             appConfig.setStatus_login(true);
                             appConfig.setUser_email(user.getString("email"));
                             appConfig.setUser_name(user.getString("name"));
                             Intent intent = new Intent(loginActivityMain.this, mainActivity.class);
                               startActivity(intent);
                               finish();
                           }else
                           {
                             Toast.makeText(getApplicationContext(),"Could Not Login Please Retry !!",Toast.LENGTH_SHORT).show();
                           }
                     }catch (Exception e)
                     {
                        Toast.makeText(getApplicationContext(),"Could Not Login Please Retry !!",Toast.LENGTH_SHORT).show();
                     }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                     Toast.makeText(getApplicationContext(),"Error Took Place Please Retry",Toast.LENGTH_SHORT).show();
                     Log.d("LOGINACTIVITY",error.toString() );
                }
            }){
                @Override
                protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                    Log.d("NETWORKREPONSE", response.headers.get("Authorization").toString().split(" ")[1]);
                    AppConfig.bearer_token = response.headers.get("Authorization").toString().split(" ")[1];
                    appConfig.set_bearer_token(response.headers.get("Authorization").toString().split(" ")[1]);
                    return super.parseNetworkResponse(response);
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/json");
                    params.put("Access-Control-Request-Method","application/json");
                    return params;
                }
            };
                try {
                    login_request.getHeaders().put("Content-Type","application/json");
                    login_request.getHeaders().put("Access-Control-Request-Method","application/json");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            AppController.getInstance().addToRequestQueue(login_request);
            return null;
        }
    }

}
