package com.example.checker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.checker.model.Task;

import java.util.ArrayList;

public class TaskAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Task> tasksList;

    public TaskAdapter(Context context, ArrayList<Task> tasksList) {
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_task, null);

        // Get the task selected
        Task task = tasksList.get(position);

        // Set its name
        TextView taskName = convertView.findViewById(R.id.taskName);
        TextView location = convertView.findViewById(R.id.location);
        TextView taskExpirationDate = convertView.findViewById(R.id.taskExpirationDate);
        //TextView status = convertView.findViewById(R.id.status);
        ImageView statusIcon = convertView.findViewById(R.id.statusIcon);
        ImageView attachIcon = convertView.findViewById(R.id.attachIcon);
        taskName.setText(task.getTaskName());
        taskExpirationDate.setText(task.getExpirationDate());
        //status.setText(task.getStatus());

        // Ask if the task is a task or entregable
        if (task.getTaskType() == 0) {
            statusIcon.setVisibility(View.VISIBLE);
        } else {
            attachIcon.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
}
