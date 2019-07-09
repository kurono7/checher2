package com.example.checker.control;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.utils.ConnectionHTTP;
import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity implements ConnectionHTTP.ConnetionCallback {
    private EditText loginMail;
    private EditText loginPassword;
    private Button loginBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize variables
        loginMail = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        // Button to login
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    // Do the process of login
    private void login(){
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        // Ask if is there connection
        if (connectionHTTP.isNetworkAvailable(LoginActivity.this)) {
            // Block windows and show the progressbar
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            // Send the request to authentification
            connectionHTTP.sendAutentification("", loginMail.getText().toString(), loginPassword.getText().toString(), "", "");
        }else{
            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResultReceived(String result, String service) {
        try {
            // Get the response
            JSONObject respon = new JSONObject(result);
            JSONObject respuesta = respon.getJSONObject("respuesta");
            String mensaje = respuesta.getString("message");
            boolean exito = respuesta.getBoolean("exito");
            if(exito){
                JSONObject data = respuesta.getJSONObject("data");
                String code = data.getString("CodigoCargo");
                String token = respon.getString("token");
                String IdUsuario = data.getString("IdUsuario");
                String Nombres = data.getString("Nombres");
                String IdPerfil= data.getString("IdPerfil");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("token", token);
                editor.putString("CodigoCargo",code);
                editor.putString("IdUsuario",IdUsuario);
                editor.putString("IdPerfil",IdPerfil);
                editor.putString("Nombres",Nombres);
                editor.apply();

                // Launch the other activity
                startActivity(new Intent(LoginActivity.this, ProjectsActivity.class));
            }else{
                Toast.makeText(getApplicationContext(),mensaje,Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(),getString(R.string.error_json),Toast.LENGTH_LONG).show();
        }
        // Set the View's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }
}
