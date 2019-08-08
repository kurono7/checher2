package com.innovacion.checker.control;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.innovacion.checker.R;
import com.innovacion.checker.model.Project;
import com.innovacion.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ProjectsActivity extends BaseTop implements ConnectionHTTP.ConnetionCallback {
    private ListView projectsList;
    private SwipeRefreshLayout swiperefresh;
    private ProgressBar progressBar;

    /**
     * Initialize UI variables. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize variables
        swiperefresh = findViewById(R.id.swiperefresh);
        projectsList = findViewById(R.id.projectsList);
        progressBar = findViewById(R.id.progressBar);

        // Get the projects
        refreshProjects();

        //Load titles from topbar
        TextView titleOne = findViewById(R.id.titleOne);
        titleOne.setText("");
        TextView titleTwo = findViewById(R.id.titleTwo);
        titleTwo.setText(R.string.projectsTitleTxt);

        //Avoid refreshing list scrolling up
        projectsList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (projectsList == null || projectsList.getChildCount() == 0) ? 0 : projectsList.getChildAt(0).getTop();
                swiperefresh.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        // Set action to profile image icon to display the popup to logout
        findViewById(R.id.optionsMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        //Refresh projects swiping
        if (swiperefresh != null) {
            swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshProjects();
                    swiperefresh.setRefreshing(false);
                }
            });
        }
    }

    /**
     * Send the request to server to get projects<br>
     * <b>pre: </b> progressBar != null. <br>
     * <b>post: </b> The request is sent to server. <br>
     */

    public void refreshProjects() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        //Ask if there is a connection available
        if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
            // Block window and show the progressbar
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // Get the data stored in preferences
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = preferences.getString("token", "");
            String IdUsuario = preferences.getString("IdUsuario", "");
            String IdPerfil = preferences.getString("IdPerfil", "");

            // Send the request to get projects
            connectionHTTP.getProjects(IdPerfil, IdUsuario, token);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
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
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP(ProjectsActivity.this);

                // Ask if is there connection
                if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                    // Block windows and show the progressbar
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Get the data stored in preferences
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
     * Receive the response of logout and get projects request from server<br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of logout and get projects request from server. result != null && result != "".
     * @param service Service requested to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        if (service.equals(ConnectionHTTP.GETPROJECTS)) {
            // Load projects and territories to start the activity to display
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
                // Get the logout authorization and start the LoginActivity
                JSONObject object = new JSONObject(result);
                boolean exito = object.getBoolean("exito");
                String message = object.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                if (exito) {
                    Intent intent = new Intent(ProjectsActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
        }
        // Set the view's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("¿Está seguro que desea salir?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
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
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_projects;
    }


}
