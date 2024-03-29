package com.innovacion.checker.control;

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

import com.innovacion.checker.R;
import com.innovacion.checker.model.Project;
import com.innovacion.checker.model.Territorie;
import com.innovacion.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class TerritoriesActivity extends BaseTop implements ConnectionHTTP.ConnetionCallback {
    private ProgressBar progressBar;

    /**
     * Initialize UI variables. <br>
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

        // Get the project that was selected in the list
        Intent intent = getIntent();
        Project project = (Project) intent.getSerializableExtra("project");
        String IdProyecto = project != null ? project.getProjectID() : null;

        // Get the data from preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String territorios = preferences.getString("territorios", "{}");

        //Set titles
        TextView territoriesTitle = findViewById(R.id.titleTwo);
        territoriesTitle.setText(R.string.territoriesTitleTxt);
        TextView projectTitle = findViewById(R.id.titleOne);
        projectTitle.setText(project.getProjectName());

        // Load the list with territories in the selected project
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

            if (territories.size() == 1) {
                // Launch the Task activity with the territorie selected
                intent = new Intent(TerritoriesActivity.this, TasksActivity.class);
                intent.putExtra("territorie", territories.get(0));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Initialize and assign action to the options menu. <br>
     * <b>pre: </b> Show popup to send to server the logout request. <br>
     * <b>post: </b> The user session is closed. <br>
     *
     * @param v View of context. v != null && v != "".
     */
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
     * Receive the response of logout and get tasks request from server<br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of logout and get tasks request from server. result != null && result != "".
     * @param service Service requested to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        if (service.equals(ConnectionHTTP.GETTASKS)) {
            try {
                // Launch the login activity if all look perfect
                JSONObject object = new JSONObject(result);
                boolean exito = object.getBoolean("exito");
                String message = object.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                if (exito) {
                    Intent intent = new Intent(TerritoriesActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                // Get the logout authorization and start the LoginActivity
                JSONObject object = new JSONObject(result);
                boolean exito = object.getBoolean("exito");
                String message = object.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                if (exito) {
                    Intent intent = new Intent(TerritoriesActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
            // Set the View's visibility back on the main UI Thread
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_territories;
    }
}
