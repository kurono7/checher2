package com.example.checker.control;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.checker.R;
import com.example.checker.model.Task;
import com.example.checker.model.Territorie;

public class DeliverableDialog extends Dialog {
    private Context context;
    private Task task;
    private Territorie territorie;
    private ProgressBar progressBar;
    private String base64;
    private TextView taskID;
    private TextView taskName;
    private TextView status;
    private TextView processName;
    private TextView subprocessName;
    private TextView expirationDate;
    private ImageView closeBtn;


    public DeliverableDialog(Context context, Task task, Territorie territorie, String base64) {
        super(context);
        this.context = context;
        this.task = task;
        this.territorie = territorie;
        this.territorie = territorie;
        this.base64 = base64;
    }

    /**
     * Initialize variables UI. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_deriverable);

        // Get preferences and save update data
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.edit().putBoolean("update", false).apply();

        // Initialize variables
        taskID = findViewById(R.id.taskID);
        taskName = findViewById(R.id.taskName);
        status = findViewById(R.id.status);
        processName = findViewById(R.id.process);
        subprocessName = findViewById(R.id.subprocess);
        expirationDate = findViewById(R.id.expirationDate);
        closeBtn = findViewById(R.id.closeBtn);
        progressBar = findViewById(R.id.progressBar);

        // Set views' data
        taskID.setText(task.getTaskID().substring(0, 8));
        processName.setText(task.getProcess());
        subprocessName.setText(task.getSubprocess());
        taskName.setText(task.getTaskName());
        expirationDate.setText(task.getExpirationDate());
        if (task.getStatus().equals("0")) {
            status.setText(context.getString(R.string.not_reportedTxt));
        } else if (task.getStatus().equals("1")) {
            status.setText(context.getString(R.string.reportedTxt));
        } else if (task.getStatus().equals("2")) {
            status.setText(context.getString(R.string.approvedTxt));
        } else {
            status.setText(context.getString(R.string.not_approvedTxt));
        }

        // Set close button action
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        if (task.getStatus().equals("2")) {
            Button attachFileBtn = findViewById(R.id.attachFileBtn);
            Button sendReportBtn = findViewById(R.id.sendReportBtn);

            attachFileBtn.setEnabled(false);
            attachFileBtn.setBackground(getContext().getDrawable(R.drawable.rounded_green_button_shape_dissabled));
            attachFileBtn.setText(getContext().getString(R.string.approvedTxt));
            sendReportBtn.setEnabled(false);
            sendReportBtn.setBackground(getContext().getDrawable(R.drawable.rounded_green_button_shape_dissabled));
            sendReportBtn.setText(getContext().getString(R.string.approvedTxt));

            findViewById(R.id.commentTxt).setVisibility(View.GONE);
            findViewById(R.id.commentTitle).setVisibility(View.GONE);
        } else {
            //TODO
        }
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public String getBase64() {
        return base64;
    }
}
