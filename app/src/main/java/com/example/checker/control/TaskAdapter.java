package com.example.checker.control;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.checker.R;
import com.example.checker.model.Task;

import java.util.ArrayList;

public class TaskAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Task> tasksList;

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

        // Set its name
        TextView taskName = convertView.findViewById(R.id.taskName);
        //TextView location = convertView.findViewById(R.id.location);
        TextView taskExpirationDate = convertView.findViewById(R.id.taskExpirationDate);
        TextView status = convertView.findViewById(R.id.status);
        ImageView corner_colored = convertView.findViewById(R.id.corner_colored);
        ImageView attachIcon = convertView.findViewById(R.id.attachIcon);
        taskName.setText(task.getTaskName());
        taskExpirationDate.setText(task.getExpirationDate());

        // Ask if the task is a task or entregable
        if (task.getTaskType() != 0) {
            attachIcon.setVisibility(View.VISIBLE);
        }

        if (task.getStatus().equals("0")) {
            status.setText(context.getString(R.string.not_reportedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_not_reported);
        } else if (task.getStatus().equals("1")) {
            status.setText(context.getString(R.string.approvedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_accepted);
            attachIcon.setVisibility(View.GONE);
        } else if (task.getStatus().equals("2")) {
            status.setText(context.getString(R.string.reportedTxt));
            corner_colored.setImageResource(R.drawable.ic_vector_corner_reported);
            attachIcon.setVisibility(View.GONE);
        }

        return convertView;
    }
}
