package com.example.checker.control;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.model.Task;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class TasksActivity extends BaseTop implements ConnectionHTTP.ConnetionCallback {


    private ListView tasksList;
    private ProgressBar progressBar;


    /**
     * Initialize variables UI. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tasksList = findViewById(R.id.tasksList);
        ImageView optionsMenu = findViewById(R.id.optionsMenu);
        progressBar = findViewById(R.id.progressBar);
        TextView projectName = findViewById(R.id.titleOne);
        TextView territorieName = findViewById(R.id.titleTwo);
        final ImageView button_filter = findViewById(R.id.button_filter);

        final ConstraintLayout filterLayout = findViewById(R.id.filter_layout);
        filterLayout.setVisibility(View.INVISIBLE);

        Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
        projectName.setText(territorie != null ? territorie.getProjectName() : null);
        territorieName.setText(territorie != null ? territorie.getTerritorieName() : null);

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        button_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!filterLayout.isShown())
                    filterLayout.setVisibility(View.VISIBLE);
                else
                    filterLayout.setVisibility(View.INVISIBLE);
            }
        });


        tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
                TaskDialog taskDialog = new TaskDialog(TasksActivity.this, (Task) tasksList.getAdapter().getItem(position), territorie);
                taskDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        boolean update = preferences.getBoolean("update", false);
                        if (update) {
                            refreshList();
                        }
                    }
                });
                taskDialog.setCancelable(true);
                taskDialog.show();
            }
        });

        refreshList();
    }


    /**
     * Send server the get task of user. <br>
     * <b>pre: </b> progressbar != null. <br>
     * <b>post: </b> The task of user are obtained. <br>
     */

    public void refreshList() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = preferences.getString("token", "");
            String code = preferences.getString("CodigoCargo", "");

            Intent intent = getIntent();
            Territorie territorie = (Territorie) intent.getSerializableExtra("territorie");
            String idProject = territorie != null ? territorie.getProjectID() : null;
            String idTerritore = territorie != null ? territorie.getTerritorieID() : null;

            connectionHTTP.getTasks(idProject, idTerritore, code, token);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Initialize . <br>
     * <b>pre: </b> Send server the close session of user. <br>
     * <b>post: </b> The session of user is closed. <br>
     *
     * @param v View of context. v != null && v != "".
     */

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP(TasksActivity.this);
                // Ask if is there connection
                if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                    // Block windows and show the progressbar
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Call the data stored in preferences
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String token = preferences.getString("token", "");
                    String IdUsuario = preferences.getString("IdUsuario", "");

                    // Send the request to logout
                    connectionHTTP.logout(IdUsuario, token);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        popup.show();
    }


    /**
     * Receive the response of get tasks and close session from server. <br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of request tasks and close session from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        if (service.equals(ConnectionHTTP.GETTASKS)) {
            ArrayList<Task> tasks = new ArrayList<>();
            try {
                JSONObject respuesta = new JSONObject(result);
                boolean exito = respuesta.getBoolean("exito");
                if (exito) {
                    JSONArray array = respuesta.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject task = array.getJSONObject(i);
                        String taskID = task.getString("IdTarea");
                        int taskType = task.getInt("TipoTarea");
                        String processID = task.getString("IdProceso");
                        String taskName = task.getString("NombreHito");
                        String status = task.getString("Estado");
                        String expirationDate = task.getString("FechaVencimiento");
                        String process = task.getString("Proceso");
                        String subprocess = task.getString("SubProceso");

                        //@SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        //expirationDate = sdf.format(expirationDate);

                        tasks.add(new Task(taskID, taskType, processID, process, subprocess, taskName, status, expirationDate));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), respuesta.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
            TaskAdapter taskAdapter = new TaskAdapter(getApplicationContext(), tasks);
            tasksList.setAdapter(taskAdapter);
        } else {
            try {
                // Launch the login activity if all look perfect
                JSONObject object = new JSONObject(result);
                boolean exito = object.getBoolean("exito");
                String message = object.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                if (exito) {
                    finish();
                    startActivity(new Intent(TasksActivity.this, LoginActivity.class));
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
        }
        // Set the View's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }


    /**
     * Receive the image that was captured by camera. <br>
     *
     * @param requestCode Request code from activity. requestCode != null && requestCode != "".
     * @param resultCode  Result code from activity sended to server. resultCode != null && resultCode != "".
     * @param data        Data sended from activity. data != null && data != "".
     */

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
        Uri uri = data.getData();
        String uriString = uri.toString();
        File myFile = new File(uriString);

        try {
            FileInputStream fileInputStreamReader = new FileInputStream(myFile);
            byte[] bytes = new byte[(int)myFile.length()];
            fileInputStreamReader.read(bytes);
            base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return base64;
    }

    /**
     * Receive the permissions that was requered by camera. <br>
     *
     * @param requestCode  Request code from activity. requestCode != null && requestCode != "".
     * @param permissions  Permissions that are requered by camera. permissions != null && permissions != "".
     * @param grantResults Permissions given. grantResults != null && grantResults != "".
     */

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TaskAdapter.MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TaskAdapter.PICK_IMAGE_CAMERA);
            } else {
                Toast.makeText(this, "Permiso de camara denegado", Toast.LENGTH_LONG).show();
            }
        }else if(requestCode == TaskAdapter.MY_GALLERY_REQUES_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*|application/pdf");
               startActivityForResult(intent, TaskAdapter.PICK_IMAGE_GALLERY);
            } else {
                Toast.makeText(this, "Permiso de archivos denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_tasks;
    }
}
