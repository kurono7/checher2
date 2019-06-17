package com.example.checker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.checker.utils.ConnectionHTTP;

public class HomeActivity extends AppCompatActivity {
    private ListView tasksList;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tasksList = findViewById(R.id.tasksList);
        progressBar = findViewById(R.id.progressBar);
    }

    public void refreshList() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP();
        if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
        // Consumir servicio


    }
}
