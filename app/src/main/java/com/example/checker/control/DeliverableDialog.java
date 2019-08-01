package com.example.checker.control;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.example.checker.R;
import com.example.checker.model.Task;

public class DeliverableDialog extends Dialog {
    private TextView taskID;
    private TextView processName;
    private TextView subprocessName;
    private TextView taskName;
    private TextView status;
    private TextView expirationDate;

    public DeliverableDialog(Context context, Bundle bundle) {
        super(context);
        Task task = (Task) bundle.getSerializable("task");
        taskID = findViewById(R.id.taskID);
        taskName = findViewById(R.id.taskName);
        status = findViewById(R.id.status);
        processName = findViewById(R.id.process);
        subprocessName = findViewById(R.id.subprocess);
        expirationDate = findViewById(R.id.expirationDate);
        taskID.setText(task.getTaskID());
        processName.setText(task.getProcess());
        taskName.setText(task.getTaskName());
        status.setText(task.getStatus());
        expirationDate.setText(task.getExpirationDate());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_deriverable);
    }
}
