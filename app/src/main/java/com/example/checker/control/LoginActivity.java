package com.example.checker.control;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity implements ConnectionHTTP.ConnetionCallback {



    private EditText loginUsername;
    private EditText loginPassword;
    private ProgressBar progressBar;



    /**
     * Initialize variables UI. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        openImageChooser();

        // Initialize variables
        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        progressBar = findViewById(R.id.progressBar);

        // Button to login
        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }



    /**
     * Send server the authentification of user. <br>
     * <b>pre: </b> progressBar != null. <br>
     * <b>post: </b> The authentification is sended to server. <br>
     */

    private void login() {
        if (checkData()) {
            final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
            // Ask if is there connection
            if (connectionHTTP.isNetworkAvailable(LoginActivity.this)) {
                // Block windows and show the progressbar
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // Send the request to authentification
                connectionHTTP.sendAutentification("", loginUsername.getText().toString(), loginPassword.getText().toString(), "", "");
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
            }
        }
    }



    /**
     * Receive the response of authentification from server. <br>
     * <b>pre: </b> progressBar != null. <br>
     * @param result Response of authentification from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        try {
            // Get the response
            JSONObject respon = new JSONObject(result);
            JSONObject respuesta = respon.getJSONObject("respuesta");
            String mensaje = respuesta.getString("message");
            boolean exito = respuesta.getBoolean("exito");
            if (exito) {
                JSONObject data = respuesta.getJSONObject("data");
                String code = data.getString("CodigoCargo");
                String token = respon.getString("token");
                String IdUsuario = data.getString("IdUsuario");
                String Nombres = data.getString("Nombres");
                String IdPerfil = data.getString("IdPerfil");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("token", token);
                editor.putString("CodigoCargo", code);
                editor.putString("IdUsuario", IdUsuario);
                editor.putString("IdPerfil", IdPerfil);
                editor.putString("Nombres", Nombres);
                editor.apply();

                // Launch the other activity
                startActivity(new Intent(LoginActivity.this, ProjectsActivity.class));
            } else {
                Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
        }
        // Set the View's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }



    /**
     * Check user name and password are completed.
     * <b>pre:</b> loginUsername != null && loginPassword != null.<br>
     * <b>post:</b> User name and password are checked.<br>
     */

    private boolean checkData() {
        boolean complete = false;
        if (loginUsername.getText().toString().isEmpty() && loginPassword.getText().toString().isEmpty()) {
            loginUsername.setError(getString(R.string.error_user_name));
            loginPassword.setError(getString(R.string.error_user_password));
        } else if (loginUsername.getText().toString().isEmpty()) {
            loginUsername.setError(getString(R.string.error_user_name));
        } else if (loginPassword.getText().toString().isEmpty()) {
            loginPassword.setError(getString(R.string.error_user_password));
        } else {
            complete = true;
        }
        return complete;
    }

















    final static int MY_CAMERA_REQUEST_CODE = 1;
    final static int MY_GALLERY_REQUES_CODE = 2;
    final static int PICK_IMAGE_CAMERA = 3;
    final static int PICK_IMAGE_GALLERY = 4;


    private void openImageChooser() {

        final CharSequence[] items = { "Tomar foto", "Seleccionar archivo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Tomar foto")) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, PICK_IMAGE_CAMERA);
                    }
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, PICK_IMAGE_CAMERA);
                } else if (items[item].equals("Seleccionar archivo")) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERY_REQUES_CODE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("application/pdf");
                        startActivityForResult(intent, PICK_IMAGE_GALLERY);
                    }
                }
            }
        });
        builder.show();

    }



    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK ) {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String taskID = preferences.getString(getString(R.string.shared_taskID), "");
            Log.e("TASK: ", taskID);

            String encodedBase64= "";
            if(requestCode == TaskAdapter.PICK_IMAGE_CAMERA){
                encodedBase64 = sendImageCaptured(data);
            }else if(requestCode == TaskAdapter.PICK_IMAGE_GALLERY){
                encodedBase64 = sendFileSelected(data);
            }

            final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
            if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                Intent intent = getIntent();
                Territorie territorie = (Territorie) intent.getSerializableExtra("territorie");
                String idProject = territorie != null ? territorie.getProjectID() : null;
                String idTerritore = territorie != null ? territorie.getTerritorieID() : null;

                String token = preferences.getString("token", "");

                connectionHTTP.setAttachTask(idProject, idTerritore, taskID, token, encodedBase64);
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.not_file), Toast.LENGTH_LONG).show();
        }
    }


    private String sendImageCaptured(Intent data){
        String base64 = "";
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) (extras != null ? extras.get("data") : null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Objects.requireNonNull(imageBitmap).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        Log.e("BASE64", base64);

        return base64;
    }

    private String sendFileSelected(Intent data){
        String base64 = "";
        // Get the Uri of the selected file
        Uri uri = Uri.parse(Objects.requireNonNull(data.getData()).toString());
        String getPath = getPath(this, uri);

        try {
            File file = new File(getPath);
            byte[] buffer = new byte[(int) file.length()];
            @SuppressWarnings("resource")
            int length = new FileInputStream(file).read(buffer);
            base64 = Base64.encodeToString(buffer, 0, length,
                    Base64.DEFAULT);
            Log.e("BASE64", base64);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return base64;
    }

    public static String getPath(final Context context, final Uri uri) {

// check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

// DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] { split[1] };

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
// MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
// File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }
    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());

    }

    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri
                .getAuthority());
    }



}
