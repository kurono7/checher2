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

import java.util.Objects;

public class TaskDialog extends Dialog implements ConnectionHTTP.ConnetionCallback {


    private Task task;
    private Button reportTaskBtn;
    private Territorie territorie;
    private ProgressBar progressBar;



    /**
     * Create a Task Dialog. <br>
     * <b>pre: </b> context != null && task != null && territorie != null. <br>
     * <b>post: </b> Task Dialog was created. <br>
     */

    TaskDialog(Context context, Task task, Territorie territorie) {
        super(context);
        this.task = task;
        this.territorie = territorie;
    }



    /**
     * Initialize variables UI. <br>
     * <b>pre: </b> context != null && task != null && territorie != null. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task);

        // Initialized variables
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.edit().putBoolean("update", false).apply();

        reportTaskBtn = findViewById(R.id.reportTaskBtn);
        TextView taskName = findViewById(R.id.taskName);
        TextView taskID = findViewById(R.id.taskID);
        TextView process = findViewById(R.id.process);
        TextView subprocess = findViewById(R.id.subprocess);
        TextView status = findViewById(R.id.status);
        TextView expirationDate = findViewById(R.id.expirationDate);

        taskName.setText(task.getTaskName());
        taskID.setText(task.getTaskID());
        process.setText(task.getProcess());
        subprocess.setText(task.getSubprocess());
        status.setText(task.getStatus());
        expirationDate.setText(task.getExpirationDate());
        progressBar = findViewById(R.id.progressBar);

        ImageButton closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if (task.getStatus().equals("1")) {
            reportTaskBtn.setEnabled(false);
            reportTaskBtn.setText(getContext().getString(R.string.reportedTxT));
            reportTaskBtn.setBackground(getContext().getDrawable(R.drawable.rounded_green_button_shape_dissabled));
            Toast.makeText(getContext(), "La tarea esta reportada", Toast.LENGTH_LONG).show();
        }

        reportTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportTaskBtn.setEnabled(false);
                reportTaskBtn.setText(getContext().getString(R.string.reportedTxT));
                updateTaskState();
            }
        });
    }



    /**
     * Send server the report state of task. <br>
     * <b>pre: </b> progressbar != null. <br>
     * <b>post: </b> The task was report. <br>
     */

    private void updateTaskState() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        // Ask if is there connection
        if (connectionHTTP.isNetworkAvailable(getContext())) {
            // Block windows and show the progressbar
            progressBar.setVisibility(View.VISIBLE);
            Objects.requireNonNull(getWindow()).setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // Call the data stored in preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            String token = preferences.getString("token", "");

            // Send the request to update task
            connectionHTTP.updateTaskState(territorie.getProjectID(), territorie.getTerritorieID(), task.getTaskID(), token);
        } else {
            Toast.makeText(getContext(), getContext().getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
        }
    }



    /**
     * Receive the response of state changed from server. <br>
     * <b>pre: </b> progressBar != null. <br>
     * @param result Response of request report task from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     */

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
        Objects.requireNonNull(getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }
}
