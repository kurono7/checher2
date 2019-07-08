package com.example.checker.control;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.model.Task;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TasksActivity extends AppCompatActivity implements ConnectionHTTP.ConnetionCallback{
    private ImageView optionsMenu;
    private ListView tasksList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        tasksList = findViewById(R.id.tasksList);
        optionsMenu = findViewById(R.id.optionsMenu);
        progressBar = findViewById(R.id.progressBar);

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
                TaskDialog taskDialog = new TaskDialog(TasksActivity.this, (Task) tasksList.getAdapter().getItem(position), territorie);
                taskDialog.setCancelable(true);
                taskDialog.show();
            }
        };
        tasksList.setOnItemClickListener(listener);

        refreshList();
    }

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

            connectionHTTP.getTasks(idProject, code, token);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection),Toast.LENGTH_LONG).show();
        }
    }

    //  Option to logout
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
                        String status = task.getString("TipoLocalizacion");
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
}
