package com.app.newssummarization;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.provider.MediaStore;
import android.widget.Toast;


/**
 * Created by Administrator on 05-May-16.
 */
public class NewsActivity extends Activity {
    ArrayList<HashMap<String, String>> CategoryArrList = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> map;
    private static final int REQUEST_PICK_FILE = 1;
    private File selectedFile;
    private String[] category;
    private String strCategory;
    private Spinner spinner_category;
    private EditText txtTitle, txtText, txtZip, txtImage;
    private Button btnSave, btnExit, btnUploadText, btnUploadZip, btnUploadImage;
    private Http http = new Http();
    private String file_path = null;
    private ProgressDialog dialog = null;
    private String upLoadServerUri = null;
    private int serverResponseCode = 0;
    private File destination;
    private static final int REQUEST_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        // Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        spinner_category = (Spinner) findViewById(R.id.cmbCategory);
        txtTitle = (EditText) findViewById(R.id.editTextTitle);
        txtText = (EditText) findViewById(R.id.txtTextFile);
        txtZip = (EditText) findViewById(R.id.txtZipFile);
        txtImage = (EditText) findViewById(R.id.txtImage);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnExit = (Button) findViewById(R.id.btnExit);
        btnUploadText = (Button) findViewById(R.id.btnUoloadTextFile);
        btnUploadZip = (Button) findViewById(R.id.btnUploadZipFile);
        btnUploadImage = (Button) findViewById(R.id.btnUploadImage);
        upLoadServerUri = "http://www.newssummarization.com/admin/api/uploadFile.php";

        btnUploadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated stub
                Intent intent = new Intent(getBaseContext(), FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);

                txtText.setText(file_path);
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), MainActivity.class);
                startActivity(i);
            }
        });

        LoadCategory();

        spinner_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int position, long id) {
                spinner_category.setSelection(position);
                strCategory = (String) spinner_category.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    private void LoadCategory() {
        String url = getString(R.string.url) + "categoryListJson.php";
        // Paste Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        try {
            JSONArray data = new JSONArray(http.getJSONUrl(url, params));
            if (data.length() > 0) {
                CategoryArrList.clear();
                category = new String[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject c = data.getJSONObject(i);
                    map = new HashMap<String, String>();
                    map.put("id", c.getString("id"));
                    map.put("name", c.getString("name"));
                    CategoryArrList.add(map);
                    category[i] = c.getString("name");
                }

                ArrayAdapter<String> dataAdapterType = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, category);
                dataAdapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner_category.setAdapter(dataAdapterType);
                String type_name = CategoryArrList.get(0).get("name");
                int innerPosition = dataAdapterType.getPosition(type_name);
                spinner_category.setSelection(innerPosition);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case REQUEST_PICK_FILE:

                    if (data.hasExtra(FilePicker.EXTRA_FILE_PATH)) {

                        selectedFile = new File
                                (data.getStringExtra(FilePicker.EXTRA_FILE_PATH));
                        file_path = selectedFile.getPath();
                    }
                    break;
            }
        }
    }

}
