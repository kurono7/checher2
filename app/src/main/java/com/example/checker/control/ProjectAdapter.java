package com.example.checker.control;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.checker.R;
import com.example.checker.model.Project;
import com.example.checker.model.Territorie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProjectAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Project> projectsList;

    ProjectAdapter(Context context, ArrayList<Project> projectsList) {
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
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
        }

        // Get the project selected
        final Project project = projectsList.get(position);

        // Catch object selected
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                String territorios = preferences.getString("territorios", "");

                // Load the list with territories in the project selected
                ArrayList<Territorie> territories = new ArrayList<>();
                try {
                    JSONObject t = new JSONObject(territorios);
                    JSONArray array = t.getJSONArray("data");

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject territorie = array.getJSONObject(i);
                        String NombreLocalizacion = territorie.getString("NombreLocalizacion");
                        String IdTerritorio = territorie.getString("IdTerritorio");
                        String IdProyect_territorie = territorie.getString("IdProyecto");

                        Territorie object = new Territorie(NombreLocalizacion, IdTerritorio, project != null ? project.getProjectName() : null, IdProyect_territorie);

                        if (IdProyect_territorie.equals(project.getProjectID())) {
                            territories.add(object);
                        }
                    }

                    // Verify and launch the tasks activity if there is only one territorie
                    if (territories.size() == 1) {
                        Intent intent = new Intent(context, TasksActivity.class);
                        intent.putExtra("territorie", territories.get(0));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        // Launch the Territorie activity with the project selected
                        Intent intent = new Intent(context, TerritoriesActivity.class);
                        intent.putExtra("project", project);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // Set its name
        TextView projectName = convertView.findViewById(R.id.projectName);
        projectName.setText(project.getProjectName());
        return convertView;
    }
}
