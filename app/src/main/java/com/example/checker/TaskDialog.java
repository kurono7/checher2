package com.example.checker;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.example.checker.model.Task;

public class TaskDialog extends Dialog {
    private TextView taskName;
    private TextView taskID;
    private TextView process;
    private TextView subprocess;
    private TextView status;
    private TextView expirationDate;
    private Task task;
    private Button reportTaskBtn;

    public TaskDialog(Context context, Task task) {
        super(context);
        this.task = task;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_task);
        reportTaskBtn = findViewById(R.id.reportTaskBtn);
        reportTaskBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reportTaskBtn.setEnabled(false);
            }
        });

        taskName = findViewById(R.id.taskName);
        taskID = findViewById(R.id.taskID);
        process = findViewById(R.id.process);
        subprocess = findViewById(R.id.subprocess);
        //status = findViewById(R.id.status);
        expirationDate = findViewById(R.id.expirationDate);

        taskName.setText(task.getTaskName());
        taskID.setText(task.getTaskID());
        process.setText(task.getProcess());
        subprocess.setText(task.getSubprocess());
        //status.setText(task.getStatus());
        expirationDate.setText(task.getExpirationDate());
    }
}
