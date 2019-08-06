package com.example.checker.control;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.checker.R;
import com.example.checker.model.Task;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;

import java.util.ArrayList;

public class TaskAdapter extends BaseAdapter implements ConnectionHTTP.ConnetionCallback{

    private Context context;
    private ArrayList<Task> tasksList;
    private ProgressBar progressBar;

    TaskAdapter(Context context, ArrayList<Task> tasksList) {
        this.context = context;
        this.tasksList = tasksList;
    }

    @Override
    public int getCount() {
        return tasksList.size();
    }

    @Override
    public Object getItem(int i) {
        return tasksList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return tasksList.get(i).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        }

        // Get the task selected
        final Task task = tasksList.get(position);

        progressBar = convertView.findViewById(R.id.progressBar);
        // Set its name
        TextView taskName = convertView.findViewById(R.id.taskName);
        //TextView location = convertView.findViewById(R.id.location);
        TextView taskExpirationDate = convertView.findViewById(R.id.taskExpirationDate);
        TextView status = convertView.findViewById(R.id.status);
        ImageView corner_colored = convertView.findViewById(R.id.corner_colored);
        ImageView attachIcon = convertView.findViewById(R.id.attachIcon);
        taskName.setText(task.getTaskName());
        taskExpirationDate.setText(task.getExpirationDate());

        ImageView message = convertView.findViewById(R.id.messageIcon);
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Territorie territorie = (Territorie) ((Activity) context).getIntent().getSerializableExtra("territorie");
                getMessage(territorie.getProjectID(), territorie.getTerritorieID(), task.getTaskID());
            }
        });

        // Ask if the task is a task or entregable
        if (task.getTaskType() == 1) {
            attachIcon.setVisibility(View.VISIBLE);
        }else{
            attachIcon.setVisibility(View.GONE);
        }

        if (task.getStatus().equals("0")) {
            status.setText(context.getString(R.string.not_reportedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_not_reported);
        } else if (task.getStatus().equals("1")) {
            status.setText(context.getString(R.string.reportedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_reported);
        } else if (task.getStatus().equals("2")) {
            status.setText(context.getString(R.string.approvedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_accepted);
        }else {
            status.setText(context.getString(R.string.not_approvedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_rejected);
            message.setVisibility(View.VISIBLE);
            status.setTextColor(ContextCompat.getColor(context,R.color.colorRejected));
        }

        return convertView;
    }

    public void getMessage(String idProject, String idTerritorie, String idTarea) {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        // Ask if is there connection
        if (connectionHTTP.isNetworkAvailable(context)) {
            // Block windows and show the progressbar
            progressBar.setVisibility(View.VISIBLE);
            ((Activity)context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // Call the data stored in preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String token = preferences.getString("token", "");

            // Send the request to get projects
            connectionHTTP.getMessage(idProject, idTerritorie, idTarea, token);
        } else {
            Toast.makeText(context, context.getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResultReceived(String result, String service) {
        // Set the View's visibility back on the main UI Thread

        if(service.equals(ConnectionHTTP.GETMESSAGE)){
            AlertDialog.Builder builder = new AlertDialog.Builder((Activity)context);
            builder.setTitle("No aprobado");
            builder.setMessage("El documento no es legible");
            builder.setCancelable(true);
            builder.create();
            builder.show();
        }

        ((Activity)context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }
}
