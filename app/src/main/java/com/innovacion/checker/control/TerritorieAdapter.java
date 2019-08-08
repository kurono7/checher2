package com.innovacion.checker.control;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.innovacion.checker.R;
import com.innovacion.checker.model.Territorie;

import java.util.ArrayList;

public class TerritorieAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Territorie> territoriesList;

    TerritorieAdapter(Context context, ArrayList<Territorie> territoriesList) {
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
        if(convertView==null)
        convertView = LayoutInflater.from(context).inflate(R.layout.item_territorie, parent,false);

        // Get the selected territorie
        final Territorie territorie = territoriesList.get(position);

        // Set actions when a territorie is selected
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Launch the Task activity with the territorie selected
                Intent intent = new Intent(context, TasksActivity.class);
                intent.putExtra("territorie", territorie);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });

        // Set the territorie name
        TextView territorieName = convertView.findViewById(R.id.territorieName);
        territorieName.setText(territorie.getTerritorieName());
        return convertView;
    }
}
