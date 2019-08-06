package com.example.checker.control;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.checker.R;
import com.example.checker.model.Task;
import com.example.checker.model.Territorie;

public class DeliverableDialog extends Dialog {

    private Task task;
    private Territorie territorie;
    private ProgressBar progressBar;
    private String base64;

    public DeliverableDialog(Context context, Task task, Territorie territorie, String base64) {
        super(context);
        this.task = task;
        this.territorie = territorie;
        this.territorie = territorie;
        this.base64 = base64;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_deriverable);

        // Initialized variables
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.edit().putBoolean("update", false).apply();

        TextView taskID = findViewById(R.id.taskID);
        TextView taskName = findViewById(R.id.taskName);
        TextView status = findViewById(R.id.status);
        TextView processName = findViewById(R.id.process);
        TextView subprocessName = findViewById(R.id.subprocess);
        TextView expirationDate = findViewById(R.id.expirationDate);
        taskID.setText(task.getTaskID().substring(0,8));
        processName.setText(task.getProcess());
        subprocessName.setText(task.getSubprocess());
        taskName.setText(task.getTaskName());
        status.setText(task.getStatus());
        expirationDate.setText(task.getExpirationDate());
        progressBar = findViewById(R.id.progressBar);

        ImageView closeBtn = findViewById(R.id.closeBtn);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if(task.getStatus().equals("2")){
            findViewById(R.id.attachFileBtn).setVisibility(View.GONE);
            findViewById(R.id.sendReportBtn).setVisibility(View.GONE);
            findViewById(R.id.commentTxt).setVisibility(View.GONE);
            findViewById(R.id.commentTitle).setVisibility(View.GONE);
        }
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getBase64() {
        return base64;
    }
}
