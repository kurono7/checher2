package com.example.checker.control;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.model.Task;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONException;
import org.json.JSONObject;

public class TaskDialog extends Dialog implements ConnectionHTTP.ConnetionCallback {
    private TextView taskName;
    private TextView taskID;
    private TextView process;
    private TextView subprocess;
    private TextView status;
    private TextView expirationDate;
    private Task task;
    private Button reportTaskBtn;
    private Territorie territorie;
    private ImageButton closeBtn;
    private ProgressBar progressBar;

    public TaskDialog(Context context, Task task, Territorie territorie) {
        super(context);
        this.task = task;
        this.territorie = territorie;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task);

        // Initialized variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.edit().putBoolean("update", false).apply();

        reportTaskBtn = findViewById(R.id.reportTaskBtn);
        taskName = findViewById(R.id.taskName);
        taskID = findViewById(R.id.taskID);
        process = findViewById(R.id.process);
        subprocess = findViewById(R.id.subprocess);
        status = findViewById(R.id.status);
        expirationDate = findViewById(R.id.expirationDate);

        taskName.setText(task.getTaskName());
        taskID.setText(task.getTaskID());
        process.setText(task.getProcess());
        subprocess.setText(task.getSubprocess());
        status.setText(task.getStatus());
        expirationDate.setText(task.getExpirationDate());
        progressBar = findViewById(R.id.progressBar);

        closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if (task.getStatus().equals("1")) {
            reportTaskBtn.setEnabled(false);
            reportTaskBtn.setText("Reportada");
            reportTaskBtn.setBackgroundDrawable(getContext().getDrawable(R.drawable.rounded_green_button_shape_dissabled));
            Toast.makeText(getContext(), "La tarea esta reportada", Toast.LENGTH_LONG).show();
        }

        reportTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportTaskBtn.setEnabled(false);
                reportTaskBtn.setText("Reportada");
                updateTaskState();
            }
        });
    }

    // Method to update the state of a task
    public void updateTaskState() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        // Ask if is there connection
        if (connectionHTTP.isNetworkAvailable(getContext())) {
            // Block windows and show the progressbar
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // Call the data stored in preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String token = preferences.getString("token", "");

            // Send the request to update task
            connectionHTTP.updateTaskState(territorie.getProjectID(), territorie.getTerritorieID(), task.getTaskID(), token);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResultReceived(String result, String service) {
        try {
            JSONObject respuesta = new JSONObject(result);
            boolean exito = respuesta.getBoolean("exito");
            if (exito) {
                Toast.makeText(getContext(), respuesta.getString("message"), Toast.LENGTH_SHORT).show();
                dismiss();
            }
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            preferences.edit().putBoolean("update", true).apply();
        } catch (JSONException e) {
            Toast.makeText(getContext(), getContext().getString(R.string.error_json), Toast.LENGTH_LONG).show();
        }

        // Set the View's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }
}
