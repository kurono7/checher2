package com.example.checker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.checker.model.Project;

import java.util.ArrayList;

public class ProjectAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Project> projectsList;

    public ProjectAdapter(Context context, ArrayList<Project> projectsList) {
        this.context = context;
        this.projectsList = projectsList;
    }

    @Override
    public int getCount() {
        return projectsList.size();
    }

    @Override
    public Object getItem(int i) {
        return projectsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return projectsList.get(i).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_project, null);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, TerritoriesActivity.class);
                intent.putExtra("task", projectsList.get(position));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        TextView projectName = convertView.findViewById(R.id.projectName);
        Project project = projectsList.get(position);
        projectName.setText(project.getProjectName());
        return convertView;
    }
}
