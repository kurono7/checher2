package com.example.checker.control;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.FileUtils;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.model.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Task> tasksList;

    public final static int MY_CAMERA_REQUEST_CODE = 1;
    public final static int PICK_IMAGE_CAMERA = 2;

    public TaskAdapter(Context context, ArrayList<Task> tasksList) {
        this.context = context;
        this.tasksList = tasksList;
    }

    @Override
    public int getCount() {
        return tasksList.size();
    }

    @Override
    public Object getItem(int i) {
        return tasksList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return tasksList.get(i).hashCode();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.item_task, null);

        // Get the task selected
        final Task task = tasksList.get(position);

        // Set its name
        TextView taskName = convertView.findViewById(R.id.taskName);
        TextView location = convertView.findViewById(R.id.location);
        TextView taskExpirationDate = convertView.findViewById(R.id.taskExpirationDate);
        TextView status = convertView.findViewById(R.id.status);
        ImageView statusIcon = convertView.findViewById(R.id.statusIcon);
        ImageView attachIcon = convertView.findViewById(R.id.attachIcon);
        taskName.setText(task.getTaskName());
        taskExpirationDate.setText(task.getExpirationDate());

        if(task.getStatus().equals("1")){
            status.setText("REPORTADO");
            statusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_vector_status_point_icon_green));
        }

        // Ask if the task is a task or entregable
        if (task.getTaskType() == 0) {
            statusIcon.setVisibility(View.VISIBLE);
        } else {
            attachIcon.setVisibility(View.VISIBLE);
            attachIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openImageChooser(parent.getContext(), task);
                }
            });
        }
        return convertView;
    }

    void openImageChooser(Context mContext, Task task) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.shared_taskID), task.getTaskID());
        editor.apply();

        if (mContext.checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ((Activity) mContext).requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, MY_CAMERA_REQUEST_CODE);
        }else{
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            ((Activity) mContext).startActivityForResult(intent, PICK_IMAGE_CAMERA);
        }
    }

}
