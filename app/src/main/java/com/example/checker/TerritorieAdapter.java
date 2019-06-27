package com.example.checker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.checker.model.Territorie;

import java.util.ArrayList;

public class TerritorieAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Territorie> territoriesList;

    public TerritorieAdapter(Context context, ArrayList<Territorie> territoriesList) {
        this.context = context;
        this.territoriesList = territoriesList;
    }

    @Override
    public int getCount() {
        return territoriesList.size();
    }

    @Override
    public Object getItem(int i) {
        return territoriesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return territoriesList.get(i).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_territorie, null);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ProjectsActivity.class);
                intent.putExtra("territorie", territoriesList.get(position));
                intent.putExtra("idProject", territoriesList.get(position).getProjectID());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        TextView territorieName = convertView.findViewById(R.id.territorieName);
        Territorie territorie = territoriesList.get(position);
        territorieName.setText(territorie.getTerritorieName());
        return convertView;
    }
}
