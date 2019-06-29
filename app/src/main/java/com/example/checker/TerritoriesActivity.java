package com.example.checker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
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
    private ImageView optionsMenu;
    private ListView territoriesList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_territories);

        // Initialize variables
        optionsMenu = findViewById(R.id.optionsMenu);
        territoriesList = findViewById(R.id.territoriesList);
        progressBar = findViewById(R.id.progressBar);
        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        // Get the project that was select in list
        Intent intent = getIntent();
        Project project = (Project) intent.getSerializableExtra("project");
        String IdProyecto = project.getProjectID();

        // Load the data of preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String territorios = preferences.getString("territorios", "{}");

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

                Territorie object = new Territorie(NombreLocalizacion, IdTerritorio, IdProyect_territorie);

                if (IdProyect_territorie.equals(IdProyecto)) {
                    territories.add(object);
                }
            }
            // Load the list
            TerritorieAdapter pAdapter = new TerritorieAdapter(getApplicationContext(), territories);
            territoriesList.setAdapter(pAdapter);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),getString(R.string.error_json),Toast.LENGTH_LONG).show();
        }
    }

    // Option to logout
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP();

                // Ask if is there connection
                if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                    // Block windows and show the progressbar
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Call the data stored in preferences
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String token = preferences.getString("token", "");
                    String IdUsuario = preferences.getString("IdUsuario", "");

                    // Send the request to logout
                    connectionHTTP.logout(IdUsuario, token);

                    // Create a Handler instance on the main thread
                    final Handler handler = new Handler();

                    // Create and start a new Thread
                    new Thread(new Runnable() {
                        int time;

                        public void run() {
                            try {
                                // Wait for the answer
                                for (time = 0; time < ConnectionHTTP.WAIT && !connectionHTTP.isFinishProcess(); time += 100) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_waiting),Toast.LENGTH_LONG).show();
                            }
                            // Now we use the Handler to post back to the main thread
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (time >= connectionHTTP.WAIT) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.time_passed), Toast.LENGTH_SHORT).show();
                                    } else if (connectionHTTP.getStatusResponse() >= 300) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_connetion), Toast.LENGTH_SHORT).show();
                                    } else if (connectionHTTP.getStatusResponse() == 200) {
                                        finish();
                                        startActivity(new Intent(TerritoriesActivity.this, LoginActivity.class));
                                    }
                                    // Set the View's visibility back on the main UI Thread
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_connection),Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        popup.show();
    }
}
