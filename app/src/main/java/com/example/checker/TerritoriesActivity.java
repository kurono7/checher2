package com.example.checker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
        refreshList();
    }

    public void refreshList() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP();
        if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = preferences.getString("token", "");
            String code = preferences.getString("CodigoCargo", "");
            connectionHTTP.getTasks("e8386888-9006-463b-a3b2-448d0a2b1fa5", code, token);
            String Nombres = preferences.getString("Nombres", "");

            // Create a Handler instance on the main thread
            final Handler handler = new Handler();

            // Create and start a new Thread
            new Thread(new Runnable() {
                int time;

                public void run() {
                    try {
                        for (time = 0; time < ConnectionHTTP.WAIT && !connectionHTTP.isFinishProcess(); time += 100) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                Toast.makeText(TerritoriesActivity.this, "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch (Exception e) {
                        // Just catch the InterruptedException
                    }
                    // Now we use the Handler to post back to the main thread
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (time >= connectionHTTP.WAIT) {
                                Toast.makeText(TerritoriesActivity.this, "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                            } else if (connectionHTTP.getStatusResponse() >= 300) {
                                Toast.makeText(TerritoriesActivity.this, "Error de conexión 300", Toast.LENGTH_SHORT).show();
                            } else if (connectionHTTP.getStatusResponse() == 200) {
                                ArrayList<Territorie> territories = new ArrayList<>();
                                try {
                                    JSONObject respuesta = new JSONObject(connectionHTTP.getResponse());
                                    JSONArray array = respuesta.getJSONArray("data");
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject territorie = array.getJSONObject(i);
                                        String territorieName = territorie.getString("NombreTerritorio");
                                        String territorieID = territorie.getString("IdTerritorio");
                                        String projectID = territorie.getString("IdProyecto");


                                        territories.add(new Territorie(territorieName, territorieID, projectID));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                TerritorieAdapter territorieAdapter = new TerritorieAdapter(getApplicationContext(), territories);
                                territoriesList.setAdapter(territorieAdapter);
                            }
                            // Set the View's visibility back on the main UI Thread
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        } else {
            Toast.makeText(TerritoriesActivity.this, "Error de conexión, not network available", Toast.LENGTH_SHORT).show();
        }
    }
}
