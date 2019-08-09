package com.innovacion.checker.control;

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

import com.innovacion.checker.R;
import com.innovacion.checker.model.Task;
import com.innovacion.checker.model.Territorie;
import com.innovacion.checker.utils.ConnectionHTTP;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TaskAdapter extends BaseAdapter implements ConnectionHTTP.ConnetionCallback {
    private Context context;
    private ArrayList<Task> tasksList;
    private ProgressBar progressBar;
    private TextView taskName;
    private TextView taskExpirationDate;
    private TextView status;
    private ImageView corner_colored;
    private ImageView attachIcon;
    private ImageView message;

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
        // Inflate the task item from layout
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        }

        // Get the selected task
        final Task task = tasksList.get(position);

        // Initialize variables
        progressBar = ((Activity) context).findViewById(R.id.progressBar);
        taskName = convertView.findViewById(R.id.taskName);
        taskExpirationDate = convertView.findViewById(R.id.taskExpirationDate);
        status = convertView.findViewById(R.id.status);
        corner_colored = convertView.findViewById(R.id.corner_colored);
        attachIcon = convertView.findViewById(R.id.attachIcon);
        message = convertView.findViewById(R.id.messageIcon);


        // Set views' data and behavior
        taskName.setText(task.getTaskName());
        taskExpirationDate.setText(task.getExpirationDate().substring(0,10));
        message.setVisibility(View.GONE);
        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Territorie territorie = (Territorie) ((Activity) context).getIntent().getSerializableExtra("territorie");
                getMessage(territorie.getProjectID(), territorie.getTerritorieID(), task.getTaskID());
            }
        });

        // Ask if the item is a task or a derivable
        if (task.getTaskType() == 1) {
            attachIcon.setVisibility(View.VISIBLE);
        } else {
            attachIcon.setVisibility(View.GONE);
        }

        if (task.getStatus().equals("0")) {
            status.setText(context.getString(R.string.not_reportedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_not_reported);
            status.setTextColor(ContextCompat.getColor(context, R.color.colorBlack));
        } else if (task.getStatus().equals("1")) {
            status.setText(context.getString(R.string.reportedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_reported);
            status.setTextColor(ContextCompat.getColor(context, R.color.colorReported));
        } else if (task.getStatus().equals("2")) {
            status.setText(context.getString(R.string.approvedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_accepted);
            status.setTextColor(ContextCompat.getColor(context, R.color.colorAccepted));
        } else {
            status.setText(context.getString(R.string.not_approvedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_rejected);
            status.setTextColor(ContextCompat.getColor(context, R.color.colorRejected));
            message.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    /**
     * Send the request to server to get rejected message<br>
     * <b>pre: </b> progressBar != null. <br>
     * <b>post: </b> The request is sent to server. <br>
     */

    public void getMessage(String idProject, String idTerritorie, String idTarea) {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        // Ask if there is a connection available
        if (connectionHTTP.isNetworkAvailable(context)) {
            // Block window and show the progressbar
            progressBar.setVisibility(View.VISIBLE);
            ((Activity) context).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // Get the data stored in preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String token = preferences.getString("token", "");

            // Send the request to get projects
            connectionHTTP.getMessage(idProject, idTerritorie, idTarea, token);
        } else {
            Toast.makeText(context, context.getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Receive the response of get message request from server<br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of authentification from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        // Set the View's visibility back on the main UI Thread
        if (service.equals(ConnectionHTTP.GETMESSAGE)) {
            try {
                JSONObject object = new JSONObject(result);
                boolean exito = object.getBoolean("exito");
                if (exito) {
                    JSONObject data = object.getJSONObject("data");

                    AlertDialog.Builder builder = new AlertDialog.Builder((Activity) context);
                    builder.setTitle("No aprobado");
                    builder.setMessage(data.getString("comentario") + "\n\n" + data.getString("por"));
                    builder.setCancelable(true);
                    builder.create();
                    builder.show();
                } else {
                    Toast.makeText(context, object.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ((Activity) context).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }
}
