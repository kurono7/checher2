package com.example.checker;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**
                 Intent intent = new Intent(context, TaskActivity.class);
                 intent.putExtra("task", tasksList.get(position));
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 context.startActivity(intent);
                 */

                Bundle args = new Bundle();
                args.putSerializable("task", tasksList.get(position));
                TaskDialog taskDialog = new TaskDialog(context, args);
                taskDialog.setCancelable(false);
                taskDialog.show();

            }
        });
        TextView taskName = convertView.findViewById(R.id.taskName);
        TextView location = convertView.findViewById(R.id.location);
        TextView taskExpirationDate = convertView.findViewById(R.id.taskExpirationDate);
        TextView status = convertView.findViewById(R.id.status);
        ImageView statusIcon = convertView.findViewById(R.id.statusIcon);
        ImageView attachIcon = convertView.findViewById(R.id.attachIcon);
        Task task = tasksList.get(position);
        taskName.setText(task.getTaskName());
        taskExpirationDate.setText(task.getExpirationDate());
        status.setText(task.getStatus());
        if (task.getTaskType() == 0) {
            statusIcon.setVisibility(View.VISIBLE);
        } else {
            attachIcon.setVisibility(View.VISIBLE);
        }
        return convertView;
    }
}
