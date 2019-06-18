package com.example.checker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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
                Intent intent = new Intent(context, TaskActivity.class);
                intent.putExtra("task", tasksList.get(position));
                context.startActivity(intent);
            }
        });
        TextView taskName = convertView.findViewById(R.id.taskName);
        TextView taskStatus = convertView.findViewById(R.id.taskStatus);
        Task task = tasksList.get(position);
        taskName.setText(task.getTaskName());
        taskStatus.setText(task.getExpirationDate());
        return convertView;
    }
}
