package com.example.checker.control;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TasksActivity extends AppCompatActivity implements ConnectionHTTP.ConnetionCallback{



    private ImageView optionsMenu;
    private ListView tasksList;
    private ProgressBar progressBar;
    private TextView projectName;
    private TextView territorieName;



    /**
     * Initialize variables UI. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        tasksList = findViewById(R.id.tasksList);
        optionsMenu = findViewById(R.id.optionsMenu);
        progressBar = findViewById(R.id.progressBar);
        projectName = findViewById(R.id.projectName);
        territorieName = findViewById(R.id.territorieName);

        Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
        projectName.setText(territorie.getProjectName());
        territorieName.setText(territorie.getTerritorieName());

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
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
                        if(update){
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
            String idProject = territorie.getProjectID();
            String idTerritore = territorie.getTerritorieID();

            connectionHTTP.getTasks(idProject, idTerritore, code, token);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection),Toast.LENGTH_LONG).show();
        }
    }



    /**
     * Initialize . <br>
     * <b>pre: </b> Send server the close session of user. <br>
     * <b>post: </b> The session of user is closed. <br>
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
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_connection),Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        popup.show();
    }



    /**
     * Receive the response of get tasks and close session from server. <br>
     * <b>pre: </b> progressBar != null. <br>
     * @param result Response of request tasks and close session from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     * @throws JSONException <br>
     *         1. If format json is misused. <br>
     */

    @Override
    public void onResultReceived(String result, String service) {
        if(service.equals(ConnectionHTTP.GETTASKS)){
            ArrayList<Task> tasks = new ArrayList<>();
            try {
                JSONObject respuesta = new JSONObject(result);
                boolean exito = respuesta.getBoolean("exito");
                if(exito) {
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

                        Date data = new Date();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        expirationDate = sdf.format(data);

                        tasks.add(new Task(taskID, taskType, processID, process, subprocess, taskName, status, expirationDate));
                    }
                }else{
                    Toast.makeText(getApplicationContext(),respuesta.getString("message"),Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(),getString(R.string.error_json),Toast.LENGTH_LONG).show();
            }
            TaskAdapter taskAdapter = new TaskAdapter(getApplicationContext(), tasks);
            tasksList.setAdapter(taskAdapter);
        }else{
            try{
                // Launch the login activity if all look perfect
                JSONObject object = new JSONObject(result);
                boolean exito = object.getBoolean("exito");
                String message = object.getString("message");
                Toast.makeText(getApplicationContext(),message,Toast.LENGTH_LONG).show();
                if(exito){
                    finish();
                    startActivity(new Intent(TasksActivity.this, LoginActivity.class));
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(),getString(R.string.error_json),Toast.LENGTH_LONG).show();
            }
        }
        // Set the View's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }



    /**
     * Receive the image that was captured by camera. <br>
     * @param requestCode Request code from activity. requestCode != null && requestCode != "".
     * @param resultCode Result code from activity sended to server. resultCode != null && resultCode != "".
     * @param data Data sended from activity. data != null && data != "".
     */

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if(resultCode == RESULT_OK && requestCode == TaskAdapter.PICK_IMAGE_CAMERA){
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream .toByteArray();
            String encodedBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
            Log.e("BASE64", encodedBase64);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String taskID = preferences.getString(getString(R.string.shared_taskID),"");
            Log.e("TASK: ", taskID);

            Toast.makeText(getApplicationContext(),"Proximamente",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getApplicationContext(),getString(R.string.not_file),Toast.LENGTH_LONG).show();
        }
    }



    /**
     * Receive the permissions that was requered by camera. <br>
     * @param requestCode Request code from activity. requestCode != null && requestCode != "".
     * @param permissions Permissions that are requered by camera. permissions != null && permissions != "".
     * @param grantResults Permissions given. grantResults != null && grantResults != "".
     */

    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == TaskAdapter.MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, TaskAdapter.PICK_IMAGE_CAMERA);
            } else {
                Toast.makeText(this, "Permiso de camara denegado", Toast.LENGTH_LONG).show();
            }
        }
    }
}
