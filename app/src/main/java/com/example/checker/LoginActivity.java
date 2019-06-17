package com.example.checker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.checker.utils.ConnectionHTTP;

public class LoginActivity extends AppCompatActivity {
    private EditText loginMail;
    private EditText loginPassword;
    private Button loginBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginMail = findViewById(R.id.loginMail);
        loginPassword = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.loginBtn);
        progressBar = findViewById(R.id.progressBar);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP();
                int time = 0;
                if (connectionHTTP.isNetworkAvailable(LoginActivity.this)) {
                    connectionHTTP.sendAutentification(loginMail.getText().toString(), loginPassword.getText().toString());
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

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
                                        Toast.makeText(LoginActivity.this, "Inténtalo más tarde", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            } catch (Exception e) {
                            } // Just catch the InterruptedException

                            // Now we use the Handler to post back to the main thread
                            handler.post(new Runnable() {
                                public void run() {

                                    if (time >= connectionHTTP.WAIT) {
                                        Toast.makeText(LoginActivity.this, "Se ha superado el tiempo de espera", Toast.LENGTH_SHORT).show();
                                    } else if (connectionHTTP.getStatusResponse() >= 300) {
                                        Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(LoginActivity.this, connectionHTTP.getResponse(), Toast.LENGTH_SHORT).show();
                                    }

                                    // Set the View's visibility back on the main UI Thread
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).start();


                }
            }
        });
    }
}
