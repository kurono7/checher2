package com.example.checker.control;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.model.Project;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TerritoriesActivity extends BaseTop implements ConnectionHTTP.ConnetionCallback {


    private ProgressBar progressBar;


    /**
     * Initialize variables UI. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize variables
        ImageView optionsMenu = findViewById(R.id.optionsMenu);
        ListView territoriesList = findViewById(R.id.territoriesList);
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
        String IdProyecto = project != null ? project.getProjectID() : null;

        // Load the data of preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String territorios = preferences.getString("territorios", "{}");

        //Load Titles
        TextView territoriesTitle = findViewById(R.id.titleTwo);
        territoriesTitle.setText(R.string.territoriesTitleTxt);
        TextView projectTitle = findViewById(R.id.titleOne);
        projectTitle.setText(project.getProjectName());

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

                if (IdProyect_territorie.equals(IdProyecto)) {
                    territories.add(object);
                }
            }
            // Load the list
            TerritorieAdapter pAdapter = new TerritorieAdapter(getApplicationContext(), territories);
            territoriesList.setAdapter(pAdapter);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Initialize . <br>
     * <b>pre: </b> Send server the close session of user. <br>
     * <b>post: </b> The session of user is closed. <br>
     *
     * @param v View of context. v != null && v != "".
     */

    // Option to logout
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP(TerritoriesActivity.this);
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
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        popup.show();
    }


    /**
     * Receive the response of close session from server. <br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of close session from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        try {
            // Launch the login activity if all look perfect
            JSONObject object = new JSONObject(result);
            boolean exito = object.getBoolean("exito");
            String message = object.getString("message");
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            if (exito) {
                finish();
                startActivity(new Intent(TerritoriesActivity.this, LoginActivity.class));
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
        }
        // Set the View's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_territories;
    }
}
