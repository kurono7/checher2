package com.example.checker;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checker.model.Task;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TaskDialog extends Dialog {
    private TextView taskName;
    private TextView taskID;
    private TextView process;
    private TextView subprocess;
    private TextView status;
    private TextView expirationDate;
    private Task task;
    private Button reportTaskBtn;

    public TaskDialog(Context context, Task task) {
        super(context);
        this.task = task;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task);
        reportTaskBtn = findViewById(R.id.reportTaskBtn);
        reportTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportTaskBtn.setEnabled(false);
                updateTaskState();
            }
        });

        taskName = findViewById(R.id.taskName);
        taskID = findViewById(R.id.taskID);
        process = findViewById(R.id.process);
        subprocess = findViewById(R.id.subprocess);
        //status = findViewById(R.id.status);
        expirationDate = findViewById(R.id.expirationDate);

        taskName.setText(task.getTaskName());
        taskID.setText(task.getTaskID());
        process.setText(task.getProcess());
        subprocess.setText(task.getSubprocess());
        //status.setText(task.getStatus());
        expirationDate.setText(task.getExpirationDate());
    }

    public void updateTaskState() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP();
        if (connectionHTTP.isNetworkAvailable(getContext())) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            //TODO
            connectionHTTP.updateTaskState("e8386888-9006-463b-a3b2-448d0a2b1fa5", "", "");

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
                                Toast.makeText(getContext(), "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                            } else if (connectionHTTP.getStatusResponse() >= 300) {
                                Toast.makeText(getContext(), "Error de conexión 300", Toast.LENGTH_SHORT).show();
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
                                //TaskAdapter taskAdapter = new TaskAdapter(getContext(), tasks);
                                //projectsList.setAdapter(taskAdapter);
                            }
                            // Set the View's visibility back on the main UI Thread
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(getContext(), "Error de conexión, not network available", Toast.LENGTH_SHORT).show();
        }
    }
}
