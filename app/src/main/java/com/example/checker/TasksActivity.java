package com.example.checker;

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

import com.example.checker.model.Task;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TasksActivity extends AppCompatActivity {
    private ImageView optionsMenu;
    private ListView tasksList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);
        tasksList = findViewById(R.id.tasksList);
        optionsMenu = findViewById(R.id.optionsMenu);

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        AdapterView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long id) {
                TaskDialog taskDialog = new TaskDialog(TasksActivity.this, (Task) tasksList.getAdapter().getItem(position));
                taskDialog.setCancelable(true);
                taskDialog.show();
            }
        };

        tasksList.setOnItemClickListener(listener);

        progressBar = findViewById(R.id.progressBar);
        refreshList();
    }

    public void refreshList() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP();
        if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = preferences.getString("token", "");
            String code = preferences.getString("CodigoCargo", "");

            Intent intent = getIntent();
            String idProject = intent.getStringExtra("idProject");

            connectionHTTP.getTasks(idProject, code, token);
            String Nombres = preferences.getString("Nombres", "");

            // Create a Handler instance on the main thread
            final Handler handler = new Handler();

            // Create and start a new Thread
            new Thread(new Runnable() {
                int time;

                public void run() {
                    try {
                        for (time = 0; time < ConnectionHTTP.WAIT && !connectionHTTP.isFinishProcess(); time += 100) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Toast.makeText(TasksActivity.this, "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        // Just catch the InterruptedException
                    }
                    // Now we use the Handler to post back to the main thread
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (time >= connectionHTTP.WAIT) {
                                Toast.makeText(TasksActivity.this, "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                            } else if (connectionHTTP.getStatusResponse() >= 300) {
                                Toast.makeText(TasksActivity.this, "Error de conexión 300", Toast.LENGTH_SHORT).show();
                            } else if (connectionHTTP.getStatusResponse() == 200) {
                                ArrayList<Task> tasks = new ArrayList<>();
                                try {
                                    JSONObject respuesta = new JSONObject(connectionHTTP.getResponse());
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
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                TaskAdapter taskAdapter = new TaskAdapter(getApplicationContext(), tasks);
                                tasksList.setAdapter(taskAdapter);
                            }
                            // Set the View's visibility back on the main UI Thread
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(TasksActivity.this, "Error de conexión, not network available", Toast.LENGTH_SHORT).show();
        }
    }

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP();
                if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String token = preferences.getString("token", "");
                    String IdUsuario = preferences.getString("IdUsuario", "");
                    connectionHTTP.logout(IdUsuario, token);

                    // Create a Handler instance on the main thread
                    final Handler handler = new Handler();

                    // Create and start a new Thread
                    new Thread(new Runnable() {
                        int time;

                        public void run() {
                            try {
                                for (time = 0; time < ConnectionHTTP.WAIT && !connectionHTTP.isFinishProcess(); time += 100) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        Toast.makeText(TasksActivity.this, "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                // Just catch the InterruptedException
                            }
                            // Now we use the Handler to post back to the main thread
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (time >= connectionHTTP.WAIT) {
                                        Toast.makeText(TasksActivity.this, "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                                    } else if (connectionHTTP.getStatusResponse() >= 300) {
                                        Toast.makeText(TasksActivity.this, "Error de conexión 300", Toast.LENGTH_SHORT).show();
                                    } else if (connectionHTTP.getStatusResponse() == 200) {
                                        finish();
                                        startActivity(new Intent(TasksActivity.this, LoginActivity.class));
                                    }
                                    // Set the View's visibility back on the main UI Thread
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                }
                return true;
            }
        });
        popup.show();
    }

}