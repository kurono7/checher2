package com.innovacion.checker.control;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;

import com.innovacion.checker.R;
import com.innovacion.checker.model.Project;
import com.innovacion.checker.model.Territorie;
import com.innovacion.checker.utils.ConnectionHTTP;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity implements ConnectionHTTP.ConnetionCallback {
    private EditText loginUsername;
    private EditText loginPassword;
    private ToggleButton showHidePassword;
    private ProgressBar progressBar;
    private Context context;
    private Switch loginRememberPassword;

    /**
     * Initialize UI variables. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize variables
        context = LoginActivity.this;
        loginUsername = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        showHidePassword = findViewById(R.id.showHidePassword);
        progressBar = findViewById(R.id.progressBar);
        loginRememberPassword = findViewById(R.id.loginRememberPassword);

        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String password = preferences.getString("password", "");
        String name = preferences.getString("name","");
        if(!password.isEmpty()){
            loginPassword.setText(password);
            loginUsername.setText(name);
            loginRememberPassword.setChecked(true);
        }

        loginRememberPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    preferences.edit().putString("password","").apply();
                    preferences.edit().putString("name","").apply();
                }
            }
        });

        // Set listener to show and hide password button
        showHidePassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (showHidePassword.isChecked()) {
                    loginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                loginPassword.setSelection(loginPassword.getText().length());
            }
        });

        // Set listener to button to login
        findViewById(R.id.loginBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        // Set listener to press enter key on SoftKeyboard to login
        loginPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            login();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });
    }

    /**
     * Send to server the users' authentification. <br>
     * <b>pre: </b> progressBar != null. <br>
     * <b>post: </b> The authentication is sended to server. <br>
     */

    private void login() {
        if (checkData()) {
            final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
            // Ask if there is a connection available
            if (connectionHTTP.isNetworkAvailable(LoginActivity.this)) {
                // Block windows and show the progressbar
                progressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                // Send the authentication request
                connectionHTTP.sendAuthentication("", loginUsername.getText().toString(), loginPassword.getText().toString(), "", "");
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Send the request to server to get projects<br>
     * <b>pre: </b> progressBar != null. <br>
     * <b>post: </b> The request is sent to server. <br>
     */

    public void getProjects() {
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
     * Receive the response of authentication and get projects request from server<br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of authentification from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        if (service.equals(ConnectionHTTP.GETPROJECTS)) {
            // Load projects and territories to start the activity to display
            ArrayList<Project> projects = new ArrayList<>();
            ArrayList<Territorie> territories = new ArrayList<>();
            try {
                JSONObject respuesta = new JSONObject(result);
                JSONObject proyectos = respuesta.getJSONObject("proyectos");
                JSONObject territorios = respuesta.getJSONObject("territorios");

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("territorios", territorios.toString());
                editor.putString("proyectos", proyectos.toString());
                editor.putBoolean("one_project", false);
                editor.apply();

                JSONArray array = proyectos.getJSONArray("data");
                JSONArray array2 = territorios.getJSONArray("data");

                for (int i = 0; i < array.length(); i++) {
                    JSONObject project = array.getJSONObject(i);
                    String IdProyecto = project.getString("IdProyecto");
                    String NombreProyecto = project.getString("NombreProyecto");

                    Project p = new Project(NombreProyecto, IdProyecto);
                    projects.add(p);
                }
                // Check if there is only one project to skip activities
                if (projects.size() == 1) {
                    editor = preferences.edit();
                    editor.putBoolean("one_project", true);
                    editor.apply();

                    for (int i = 0; i < array2.length(); i++) {
                        JSONObject territorie = array2.getJSONObject(i);
                        String NombreLocalizacion = territorie.getString("NombreLocalizacion");
                        String IdTerritorio = territorie.getString("IdTerritorio");
                        String IdProyect_territorie = territorie.getString("IdProyecto");

                        Territorie object = new Territorie(NombreLocalizacion, IdTerritorio, projects.get(0) != null ? projects.get(0).getProjectName() : null, IdProyect_territorie);

                        if (IdProyect_territorie.equals(projects.get(0).getProjectID())) {
                            territories.add(object);
                        }
                    }
                    // Check if there is only one territorie to go directly to TasksActivity
                    if (territories.size() == 1) {
                        Intent intent = new Intent(context, TasksActivity.class);
                        intent.putExtra("territorie", territories.get(0));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        // Launch the TerritorieActivity with the project selected
                        Intent intent = new Intent(context, TerritoriesActivity.class);
                        intent.putExtra("project", projects.get(0));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                } else {
                    // Launch the ProjectsActivity if there are more than one project
                    startActivity(new Intent(LoginActivity.this, ProjectsActivity.class));
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                loginUsername.setText("");
                loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                loginUsername.requestFocus();
            }

            // Set the View's visibility back on the main UI Thread
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            progressBar.setVisibility(View.GONE);

        } else {
            try {
                // Get the profile data from authentication response
                JSONObject respon = new JSONObject(result);
                JSONObject respuesta = respon.getJSONObject("respuesta");
                String mensaje = respuesta.getString("message");
                boolean exito = respuesta.getBoolean("exito");
                if (exito) {
                    JSONObject data = respuesta.getJSONObject("data");
                    String code = data.getString("CodigoCargo");
                    String token = respon.getString("token");
                    String IdUsuario = data.getString("IdUsuario");
                    String Nombres = data.getString("Nombres");
                    String IdPerfil = data.getString("IdPerfil");
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("token", token);
                    editor.putString("CodigoCargo", code);
                    editor.putString("IdUsuario", IdUsuario);
                    editor.putString("IdPerfil", IdPerfil);
                    editor.putString("Nombres", Nombres);
                    editor.apply();

                    if(loginRememberPassword.isChecked()){
                        preferences.edit().putString("password", loginPassword.getText().toString()).apply();
                        preferences.edit().putString("name",loginUsername.getText().toString()).apply();
                    }else{
                        preferences.edit().putString("password","").apply();
                        preferences.edit().putString("name","").apply();
                    }

                    getProjects();
                } else {
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    loginUsername.setText("");
                    loginPassword.setText("");
                    loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    loginUsername.requestFocus();

                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                loginUsername.setText("");
                loginPassword.setText("");
                loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                loginUsername.requestFocus();
            }

        }
    }

    /**
     * Check if user name and password are completed.
     * <b>pre:</b> loginUsername != null && loginPassword != null.<br>
     * <b>post:</b> User name and password are checked.<br>
     */

    private boolean checkData() {
        boolean complete = false;
        if (loginUsername.getText().toString().isEmpty() && loginPassword.getText().toString().isEmpty()) {
            loginUsername.setError(getString(R.string.error_user_name));
            loginPassword.setError(getString(R.string.error_user_password));
        } else if (loginUsername.getText().toString().isEmpty()) {
            loginUsername.setError(getString(R.string.error_user_name));
        } else if (loginPassword.getText().toString().isEmpty()) {
            loginPassword.setError(getString(R.string.error_user_password));
        } else {
            complete = true;
        }
        return complete;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }
}