package com.example.checker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.checker.utils.ConnectionHTTP;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private EditText loginMail;
    private EditText loginPassword;
    private Button loginBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginMail = findViewById(R.id.loginUsername);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP();

                if (connectionHTTP.isNetworkAvailable(LoginActivity.this)) {
                    connectionHTTP.sendAutentification("", loginMail.getText().toString(), loginPassword.getText().toString(), "", "");
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Create a Handler instance on the main thread
                    final Handler handler = new Handler();
                    // Create and start a new Thread
                    new Thread(new Runnable() {
                        int time = 0;

                        public void run() {
                            try {
                                for (time = 0; time < ConnectionHTTP.WAIT && !connectionHTTP.isFinishProcess(); time += 100) {
                                    try {
                                        Thread.sleep(100);
                                    } catch (InterruptedException e) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.try_later), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } catch (Exception e) {
                                Toast.makeText(getApplicationContext(), getString(R.string.error_waiting), Toast.LENGTH_LONG).show();
                            }

                            // Now we use the Handler to post back to the main thread
                            handler.post(new Runnable() {
                                public void run() {
                                    if (time >= ConnectionHTTP.WAIT) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.time_passed), Toast.LENGTH_SHORT).show();
                                    } else if (connectionHTTP.getStatusResponse() >= 300) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.error_connetion), Toast.LENGTH_SHORT).show();
                                    } else {

                                        try {
                                            JSONObject respon = new JSONObject(connectionHTTP.getResponse());
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

                                                startActivity(new Intent(LoginActivity.this, ProjectsActivity.class));
                                            } else {
                                                Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_LONG).show();
                                            }
                                        } catch (JSONException e) {
                                            Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    // Set the View's visibility back on the main UI Thread
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

