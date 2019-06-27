package com.example.checker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.example.checker.model.Task;

public class TaskActivity extends AppCompatActivity {
    private TextView taskID;
    private TextView processID;
    private TextView taskName;
    private TextView status;
    private TextView expirationDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        Task task = (Task) getIntent().getSerializableExtra("task");
        taskID = findViewById(R.id.taskID);
        processID = findViewById(R.id.processID);
        taskName = findViewById(R.id.taskName);
        status = findViewById(R.id.status);
        expirationDate = findViewById(R.id.expirationDate);

        taskID.setText(task.getTaskID());
        processID.setText(task.getProcessID());
        taskName.setText(task.getTaskName());
        status.setText(task.getStatus());
        expirationDate.setText(task.getExpirationDate());
    }


}
