package com.example.checker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.checker.model.Project;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TerritoriesActivity extends AppCompatActivity {
    private ListView territoriesList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_territories);
        territoriesList = findViewById(R.id.territoriesList);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        String IdProyecto = intent.getStringExtra("IdProyecto");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String territorios = preferences.getString("territorios","");

        ArrayList<Territorie> territories = new ArrayList<>();
        try{
        JSONObject t = new JSONObject(territorios);
        JSONArray array = t.getJSONArray("data");

            for (int i = 0; i < array.length(); i++) {
                JSONObject territorie = array.getJSONObject(i);
                String NombreLocalizacion = territorie.getString("NombreLocalizacion");
                String IdTerritorio = territorie.getString("IdTerritorio");
                String IdProyect = territorie.getString("IdProyecto");

                Territorie p = new Territorie(NombreLocalizacion, IdTerritorio,IdProyect);

                if(IdProyect.equals(IdProyecto)){
                    territories.add(p);
                }
            }
            TerritorieAdapter pAdapter = new TerritorieAdapter(getApplicationContext(), territories);
            territoriesList.setAdapter(pAdapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
