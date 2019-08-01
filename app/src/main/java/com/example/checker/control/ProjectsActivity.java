package com.example.checker.control;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.model.Project;
import com.example.checker.model.Task;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ProjectsActivity extends BaseTop implements ConnectionHTTP.ConnetionCallback {


    private ListView projectsList;
    private ProgressBar progressBar;

    /**
     * Initialize variables UI. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialize variables
        projectsList = findViewById(R.id.projectsList);
        progressBar = findViewById(R.id.progressBar);

        // Option to logout
        findViewById(R.id.optionsMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        //Load titles
        TextView titleOne = findViewById(R.id.titleOne);
        titleOne.setText("");
        TextView titleTwo = findViewById(R.id.titleTwo);
        titleTwo.setText(R.string.projectsTitleTxt);


        // Get the projects
        refreshProjects();
    }


    /**
     * Send server the get projects of user. <br>
     * <b>pre: </b> progressBar != null. <br>
     * <b>post: </b> The projects of user are obtained. <br>
     */

    public void refreshProjects() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        // Ask if is there connection
        if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
            // Block windows and show the progressbar
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // Call the data stored in preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = preferences.getString("token", "");
            String IdUsuario = preferences.getString("IdUsuario", "");
            String IdPerfil = preferences.getString("IdPerfil", "");

            // Send the request to get projects
            connectionHTTP.getproyects(IdPerfil, IdUsuario, token);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Initialize . <br>
     * <b>pre: </b> Send server the close session of user. <br>
     * <b>post: </b> The session of user is closed. <br>
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
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP(ProjectsActivity.this);

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
     * Receive the response of get projects and close session from server. <br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of request projects and close session from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        if (service.equals(ConnectionHTTP.GETPROYECTS)) {
            // If all look perfect so load projects
            ArrayList<Project> projects = new ArrayList<>();
            try {
                JSONObject respuesta = new JSONObject(result);
                JSONObject proyectos = respuesta.getJSONObject("proyectos");
                JSONObject territorios = respuesta.getJSONObject("territorios");

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("territorios", territorios.toString());
                editor.putString("proyectos", proyectos.toString());
                editor.apply();

                JSONArray array = proyectos.getJSONArray("data");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject project = array.getJSONObject(i);
                    String IdProyecto = project.getString("IdProyecto");
                    String NombreProyecto = project.getString("NombreProyecto");

                    Project p = new Project(NombreProyecto, IdProyecto);
                    projects.add(p);
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
            // Load the list with projects
            ProjectAdapter pAdapter = new ProjectAdapter(getApplicationContext(), projects);
            projectsList.setAdapter(pAdapter);
        } else {
            try {
                // Launch the login activity if all look perfect
                JSONObject object = new JSONObject(result);
                boolean exito = object.getBoolean("exito");
                String message = object.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                if (exito) {
                    finish();
                    startActivity(new Intent(ProjectsActivity.this, LoginActivity.class));
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
        }
        // Set the View's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_projects;
    }






}
