package com.app.newssummarization;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private String strText;
    private Spinner spinner_category;
    private EditText txtTitle, txtText, txtZip, txtImage;
    private Button btnSave, btnExit, btnUploadText, btnUploadZip, btnUploadImage;
    private Http http = new Http();
    private String file_path = null;
/*    private ProgressDialog dialog = null;*/
    private String upLoadServerUrl = null;
    private int serverResponseCode = 0;
    private File destination;
    private static final int REQUEST_IMAGE = 100;
    private String file_type = "", file_text = "", file_zip = "", file_image = "";
    private String[] fileSplite;
/*    final File sdcard = Environment.getExternalStorageDirectory(); // /mnt/sdcard/*/

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
        upLoadServerUrl = "http://www.newssummarization.com/admin/api/";
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        btnUploadText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated stub
                Intent intent = new Intent(getBaseContext(), FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);

                file_type = "text";
            }
        });
        btnUploadZip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated stub
                Intent intent = new Intent(getBaseContext(), FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);

                file_type = "zip";
            }
        });
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated stub
                Intent intent = new Intent(getBaseContext(), FilePicker.class);
                startActivityForResult(intent, REQUEST_PICK_FILE);

                file_type = "image";
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean status = CheckData();
                if (status) {
                    //dialog = ProgressDialog.show(getBaseContext(), "", "Loading ...", true);
                    SaveData();
                    //dialog.dismiss();
                } else {
                    builder.setTitle("Warning");
                    builder.setMessage("Please fill in complete data. !")
                            .setCancelable(true)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).show();
                }
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

    private void SaveData() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String url = getString(R.string.url) + "saveNews.php";
        String msgStatus = "";
        // Paste Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("title", txtTitle.getText().toString().trim()));
        params.add(new BasicNameValuePair("categories", strCategory));
        params.add(new BasicNameValuePair("detail", strText));
        params.add(new BasicNameValuePair("file_zip", file_zip));
        params.add(new BasicNameValuePair("picture", file_image));

        try {
            JSONArray data = new JSONArray(http.getJSONUrl(url, params));
            if (data.length() > 0) {
                JSONObject c = data.getJSONObject(0);
                msgStatus = c.getString("error");
                if ("Success".equals(msgStatus)) {
                    uploadFiletoServer(txtText.getText().toString().trim(), upLoadServerUrl + "uploadFile.php");
                    uploadFiletoServer(txtZip.getText().toString().trim(), upLoadServerUrl + "uploadZip.php");
                    uploadFiletoServer(txtImage.getText().toString().trim(), upLoadServerUrl + "uploadImg.php");
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            msgStatus = e.getMessage();
        }
        builder.setTitle("Status");
        builder.setMessage(msgStatus)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

    private boolean CheckData() {
        boolean status = false;
        if (!"".equals(txtTitle.getText().toString().trim()) && !"".equals(file_text) && !"".equals(file_zip) && !"".equals(file_image)) {

            /*** Read Text File in SD Card ***/
            try {

                //String path = sdcard + txtText.getText().toString().trim();
                String path = txtText.getText().toString().trim();
                File file = new File(path);
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                String line;
                while ((line = br.readLine()) != null) {
                    strText +=  line + "<br />";
                    //System.out.println(line);
                }
                strText = strText.replace("'", "");
                br.close();
                file = null;

              /*  txtV.setText(line);*/
                status = true;
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(getBaseContext(), "Failed! = " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                status = false;
            }

        }
        return status;
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
                        switch (file_type) {
                            case "text":
                                txtText.setText(file_path);
                                fileSplite = file_path.split("/");
                                file_text = fileSplite[fileSplite.length - 1];
                                btnUploadText.setBackgroundColor(Color.GREEN);
                                break;
                            case "zip":
                                txtZip.setText(file_path);
                                fileSplite = file_path.split("/");
                                file_zip = fileSplite[fileSplite.length - 1];
                                btnUploadZip.setBackgroundColor(Color.GREEN);
                                break;
                            case "image":
                                txtImage.setText(file_path);
                                fileSplite = file_path.split("/");
                                file_image = fileSplite[fileSplite.length - 1];
                                btnUploadImage.setBackgroundColor(Color.GREEN);
                                break;
                        }
                    }
                    break;
            }
        }
    }

    public static boolean uploadFiletoServer(String strSDPath, String strUrlServer) {

        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        int resCode = 0;
        String resMessage = "";

        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";

        try {
            File file = new File(strSDPath);
            if (!file.exists()) {
                return false;
            }

            FileInputStream fileInputStream = new FileInputStream(new File(strSDPath));

            URL url = new URL(strUrlServer);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream outputStream = new DataOutputStream(conn.getOutputStream());
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes(
                    "Content-Disposition: form-data; name=\"filUpload\";filename=\"" + strSDPath + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];

            // Read file
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Response Code and Message
            resCode = conn.getResponseCode();
            if (resCode == HttpURLConnection.HTTP_OK) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                int read = 0;
                while ((read = is.read()) != -1) {
                    bos.write(read);
                }
                byte[] result = bos.toByteArray();
                bos.close();

                resMessage = new String(result);

            }

            fileInputStream.close();
            outputStream.flush();
            outputStream.close();

            return true;

        } catch (Exception ex) {
            // Exception handling
            return false;
        }
    }

}
