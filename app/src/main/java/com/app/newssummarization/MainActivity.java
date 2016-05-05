package com.app.newssummarization;

import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends Activity {
    private Button btLogin;
    private EditText txtUsername, txtPassword;
    private Http http = new Http();
    /*    private String strUsername = "";
        private String strPassword = "";
        private String strType = "";*/
    private String strError = "Please connection internet!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        btLogin = (Button) findViewById(R.id.btnLogin);
        txtUsername = (EditText) findViewById(R.id.editTextUsername);
        txtPassword = (EditText) findViewById(R.id.editTextPassword);


        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean status = OnLogin();
                if (status) {
                    Toast.makeText(getApplicationContext(), strError, Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getBaseContext(), NewsActivity.class);
                    startActivity(i);
                } else {
                    Toast.makeText(getApplicationContext(), strError, Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private boolean OnLogin() {
/*        final AlertDialog.Builder ad = new AlertDialog.Builder(this);*/
        Boolean ststusLogin = false;
        String Error = "1";
        String url = getString(R.string.url) + "checkLoginJson.php";
        // Paste Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("username", txtUsername.getText()
                .toString().trim()));
        params.add(new BasicNameValuePair("password", txtPassword.getText()
                .toString().trim()));
        try {
            JSONArray data = new JSONArray(http.getJSONUrl(url, params));
            if (data.length() > 0) {
                JSONObject c = data.getJSONObject(0);
                Error = c.getString("error");
            }

            if ("0".equals(Error)) {
                ststusLogin = true;
                strError = "Login Success...";
            } else {
                ststusLogin = false;
                strError = "Username or Password is incorrect !";
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            ststusLogin = false;
            strError = e.getMessage();
        }
        return ststusLogin;
    }

}