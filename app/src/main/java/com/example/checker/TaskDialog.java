package com.example.checker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.example.checker.model.Task;

public class TaskDialog extends Dialog {
    private TextView taskID;
    private TextView processID;
    private TextView taskName;
    private TextView status;
    private TextView expirationDate;

    public TaskDialog(Context context, Bundle bundle) {
        super(context);
        Task task = (Task) bundle.getSerializable("task");
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task);
    }
}
