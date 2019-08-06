package com.example.checker.control;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.checker.R;
import com.example.checker.model.Task;
import com.example.checker.model.Territorie;
import com.example.checker.utils.ConnectionHTTP;
import com.example.checker.utils.ExternalStorage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class TasksActivity extends BaseTop implements ConnectionHTTP.ConnetionCallback {

    private ListView tasksList;
    private ArrayList<Task> tasks;
    private ProgressBar progressBar;
    private EditText searchBar;
    private ImageView searchBtn;
    private int positionItemSelected;
    private DeliverableDialog deliverableDialog;
    private SwipeRefreshLayout swiperefresh;

    final static String PDF = ".pdf";
    final static String PNG = ".png";
    final static String XSLX = ".xslx";
    final static String XSL = ".xsl";
    final static String JPG = ".jpg";
    final static int MY_CAMERA_REQUEST_CODE = 1;
    final static int MY_GALLERY_REQUES_CODE = 2;
    final static int PICK_IMAGE_CAMERA = 3;
    final static int PICK_IMAGE_GALLERY = 4;

    /**
     * Initialize variables UI. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tasksList = findViewById(R.id.tasksList);
        searchBar = findViewById(R.id.searchBar);
        searchBtn = findViewById(R.id.searchBtn);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchByName();
                if (charSequence.length() > 0) {
                    searchBtn.setImageResource(R.drawable.ic_vector_close_green_icon);
                    searchBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searchBar.setText("");
                        }
                    });
                } else {
                    searchBtn.setImageResource(R.drawable.ic_vector_search_icon);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        progressBar = findViewById(R.id.progressBar);
        ImageView optionsMenu = findViewById(R.id.optionsMenu);
        TextView projectName = findViewById(R.id.titleOne);
        TextView territorieName = findViewById(R.id.titleTwo);

        final ImageView button_filter = findViewById(R.id.button_filter);
        final ConstraintLayout filterLayout = findViewById(R.id.filter_layout);
        filterLayout.setVisibility(View.INVISIBLE);

        Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
        projectName.setText(territorie != null ? territorie.getProjectName() : null);
        territorieName.setText(territorie != null ? territorie.getTerritorieName() : null);

        optionsMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        button_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!filterLayout.isShown())
                    filterLayout.setVisibility(View.VISIBLE);
                else
                    filterLayout.setVisibility(View.INVISIBLE);
            }
        });

        //Refreshing tasks
        swiperefresh = findViewById(R.id.swiperefresh);
        if (swiperefresh != null) {
            swiperefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshList();
                    hideSoftKeyBoard();
                    searchBar.setText("");
                    filterLayout.setVisibility(View.INVISIBLE);
                    swiperefresh.setRefreshing(false);
                }
            });
        }

        //Cleaning filters
        final CheckBox filter_not_reported = filterLayout.findViewById(R.id.filter_not_reported);
        final CheckBox filter_reported = filterLayout.findViewById(R.id.filter_reported);
        final CheckBox filter_approved = filterLayout.findViewById(R.id.filter_approved);
        final CheckBox filter_not_approved = filterLayout.findViewById(R.id.filter_not_approved);
        Button filter_clear = filterLayout.findViewById(R.id.filter_clear);
        filter_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filter_not_reported.setChecked(false);
                filter_reported.setChecked(false);
                filter_approved.setChecked(false);
                filter_not_approved.setChecked(false);
                TaskAdapter taskAdapter = new TaskAdapter(TasksActivity.this, tasks);
                tasksList.setAdapter(taskAdapter);
                filterLayout.setVisibility(View.INVISIBLE);
            }
        });

        //Filtering results
        Button filter_find = filterLayout.findViewById(R.id.filter_find);
        filter_find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filter_not_reported.isChecked() || filter_reported.isChecked() || filter_not_approved.isChecked() || filter_approved.isChecked()) {
                    ArrayList<Task> tasksFiltered = new ArrayList<>();
                    String taskStatus = "";
                    for (int i = 0; i < tasks.size(); i++) {
                        Task task = tasks.get(i);
                        taskStatus = task.getStatus();
                        if (filter_not_reported.isChecked() && taskStatus.equals("0")) {
                            tasksFiltered.add(task);
                        } else if (filter_approved.isChecked() && taskStatus.equals("1")) {
                            tasksFiltered.add(task);
                        } else if (filter_reported.isChecked() && taskStatus.equals("2")) {
                            tasksFiltered.add(task);
                        } else if (filter_not_approved.isChecked() && taskStatus.equals("3")) {
                            tasksFiltered.add(task);
                        }
                    }

                    TaskAdapter taskAdapter = new TaskAdapter(TasksActivity.this, tasksFiltered);
                    tasksList.setAdapter(taskAdapter);
                    if (tasksFiltered.size() < 1)
                        Toast.makeText(TasksActivity.this, R.string.error_no_results, Toast.LENGTH_LONG).show();

                } else {
                    TaskAdapter taskAdapter = new TaskAdapter(TasksActivity.this, tasks);
                    tasksList.setAdapter(taskAdapter);
                }
                filterLayout.setVisibility(View.INVISIBLE);
            }
        });

        tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                filterLayout.setVisibility(View.INVISIBLE);
                final Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
                final Task task = (Task) tasksList.getAdapter().getItem(position);
                if (task.getTaskType() == 1) {
                    positionItemSelected = position;
                    deliverableDialog = new DeliverableDialog(TasksActivity.this, task, territorie, "");
                    deliverableDialog.setCancelable(true);
                    deliverableDialog.show();

                    Button attachFileBtn = deliverableDialog.findViewById(R.id.attachFileBtn);
                    attachFileBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openImageChooser();
                        }
                    });

                    Button sendReportBtn = deliverableDialog.findViewById(R.id.sendReportBtn);
                    sendReportBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String base64 = deliverableDialog.getBase64();

                            if (base64 != null && !base64.isEmpty()) {
                                final ConnectionHTTP connectionHTTP = new ConnectionHTTP(TasksActivity.this);
                                if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    String token = preferences.getString("token", "");

                                    connectionHTTP.setAttachTask(territorie.getProjectID(), territorie.getTerritorieID(), task.getTaskID(), token, base64, ((EditText) deliverableDialog.findViewById(R.id.commentTxt)).getText().toString());
                                    deliverableDialog.dismiss();
                                } else {
                                    Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Debe seleccionar un archivo", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    deliverableDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            boolean update = preferences.getBoolean("update", false);
                            if (update) {
                                refreshList();
                            }
                        }
                    });

                } else {
                    TaskDialog taskDialog = new TaskDialog(TasksActivity.this, (Task) tasksList.getAdapter().getItem(position), territorie);
                    taskDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            boolean update = preferences.getBoolean("update", false);
                            if (update) {
                                refreshList();
                            }
                        }
                    });
                    taskDialog.setCancelable(true);
                    taskDialog.show();
                }


            }
        });

        //Avoid refreshing list when scrolls up
        tasksList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (tasksList == null || tasksList.getChildCount() == 0) ? 0 : tasksList.getChildAt(0).getTop();
                swiperefresh.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        refreshList();
    }


    final public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }

    final public void searchByName() {
        ArrayList<Task> tasksFiltered = new ArrayList<>();
        String taskName = "";
        for (int k = 0; tasks!=null && k < tasks.size(); k++) {
            Task task = tasks.get(k);
            if (containsIgnoreCase(task.getTaskName(), searchBar.getText().toString())) {
                tasksFiltered.add(task);
            }
        }
        TaskAdapter taskAdapter = new TaskAdapter(TasksActivity.this, tasksFiltered);
        tasksList.setAdapter(taskAdapter);
    }

    final public void hideSoftKeyBoard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Send server the get task of user. <br>
     * <b>pre: </b> progressbar != null. <br>
     * <b>post: </b> The task of user are obtained. <br>
     */

    public void refreshList() {
        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(this);
        if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String token = preferences.getString("token", "");
            String code = preferences.getString("CodigoCargo", "");

            Intent intent = getIntent();
            Territorie territorie = (Territorie) intent.getSerializableExtra("territorie");
            String idProject = territorie != null ? territorie.getProjectID() : null;
            String idTerritore = territorie != null ? territorie.getTerritorieID() : null;

            connectionHTTP.getTasks(idProject, idTerritore, code, token);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Initialize . <br>
     * <b>pre: </b> Send server the close session of user. <br>
     * <b>post: </b> The session of user is closed. <br>
     *
     * @param v View of context. v != null && v != "".
     */

    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.options_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                final ConnectionHTTP connectionHTTP = new ConnectionHTTP(TasksActivity.this);
                // Ask if is there connection
                if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                    // Block windows and show the progressbar
                    progressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    // Call the data stored in preferences
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    String token = preferences.getString("token", "");
                    String IdUsuario = preferences.getString("IdUsuario", "");

                    // Send the request to logout
                    connectionHTTP.logout(IdUsuario, token);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        popup.show();
    }


    /**
     * Receive the response of get tasks and close session from server. <br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of request tasks and close session from server. result != null && result != "".
     * @param service Service sended to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        if (service.equals(ConnectionHTTP.ATTACH_TASK)) {
            try {
                JSONObject respuesta = new JSONObject(result);
                boolean exito = respuesta.getBoolean("exito");

                if (exito) {
                    refreshList();
                }

                Toast.makeText(TasksActivity.this, respuesta.getString("message"), Toast.LENGTH_LONG).show();
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
        } else if (service.equals(ConnectionHTTP.GETTASKS)) {
            tasks = new ArrayList<>();
            try {
                JSONObject respuesta = new JSONObject(result);
                boolean exito = respuesta.getBoolean("exito");
                if (exito) {
                    JSONArray array = respuesta.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject task = array.getJSONObject(i);
                        String taskID = task.getString("IdTarea");
                        int taskType = task.getInt("TipoTarea");
                        String processID = task.getString("IdProceso");
                        String taskName = task.getString("NombreHito");
                        String status = task.getString("Estado");
                        String expirationDate = task.getString("FechaVencimiento");
                        String process = task.getString("Proceso");
                        String subprocess = task.getString("SubProceso");
                        String extensionArchivo = task.getString("ExtensionArchivo");

                        tasks.add(new Task(taskID, taskType, processID, process, subprocess, taskName, status, expirationDate, extensionArchivo));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), respuesta.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
            TaskAdapter taskAdapter = new TaskAdapter(TasksActivity.this, tasks);
            tasksList.setAdapter(taskAdapter);
        } else if (service.equals(ConnectionHTTP.SIGNOUT)) {
            try {
                // Launch the login activity if all look perfect
                JSONObject object = new JSONObject(result);
                boolean exito = object.getBoolean("exito");
                String message = object.getString("message");
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                if (exito) {
                    finish();
                }
            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_json), Toast.LENGTH_LONG).show();
            }
        }
        // Set the View's visibility back on the main UI Thread
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.GONE);
    }


    /**
     * Receive the image that was captured by camera. <br>
     *
     * @param requestCode Request code from activity. requestCode != null && requestCode != "".
     * @param resultCode  Result code from activity sended to server. resultCode != null && resultCode != "".
     * @param data        Data sended from activity. data != null && data != "".
     */

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            String encodedBase64 = "";
            if (requestCode == PICK_IMAGE_CAMERA) {
                encodedBase64 = sendImageCaptured(data);
            } else if (requestCode == PICK_IMAGE_GALLERY) {
                encodedBase64 = sendFileSelected(data);
            }

            final Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
            final Task task = (Task) tasksList.getAdapter().getItem(positionItemSelected);


            if (task.getTaskType() == 1) {
                deliverableDialog.setBase64(encodedBase64);
                Button b = deliverableDialog.findViewById(R.id.attachFileBtn);
                b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_vector_attach_icon, 0, 0, 0);
            } else {
                TaskDialog taskDialog = new TaskDialog(TasksActivity.this, task, territorie);
                taskDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        boolean update = preferences.getBoolean("update", false);
                        if (update) {
                            refreshList();
                        }
                    }
                });
                taskDialog.setCancelable(true);
                taskDialog.show();
            }
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.not_file), Toast.LENGTH_LONG).show();
        }
    }

    private String sendImageCaptured(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) (extras != null ? extras.get("data") : null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Objects.requireNonNull(imageBitmap).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);

        return base64;
    }

    private String sendFileSelected(Intent data) {
        String base64 = "";
        // Get the Uri of the selected file
        Uri uri = Uri.parse(Objects.requireNonNull(data.getData()).toString());
        String getPath = ExternalStorage.getPath(this, uri);

        try {
            File file = new File(getPath);
            byte[] buffer = new byte[(int) file.length()];
            @SuppressWarnings("resource")
            int length = new FileInputStream(file).read(buffer);
            base64 = Base64.encodeToString(buffer, 0, length,
                    Base64.DEFAULT);
            Log.e("BASE64", base64);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return base64;
    }

    /**
     * Receive the permissions that was requered by camera. <br>
     *
     * @param requestCode  Request code from activity. requestCode != null && requestCode != "".
     * @param permissions  Permissions that are requered by camera. permissions != null && permissions != "".
     * @param grantResults Permissions given. grantResults != null && grantResults != "".
     */

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, PICK_IMAGE_CAMERA);
            } else {
                Toast.makeText(this, "Permiso de camara denegado", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_GALLERY_REQUES_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                String[] taskExtension = ((Task) tasksList.getAdapter().getItem(positionItemSelected)).getExtensionArchivo().split(", ");
                ArrayList<String> array = new ArrayList<>();

                for (int i = 0; i < taskExtension.length; i++) {
                    if (taskExtension[i].equals(PDF)) {
                        array.add("application/pdf");
                    } else if (taskExtension[i].equals(XSL)) {
                        array.add("application/vnd.ms-excel");
                    } else if (taskExtension[i].equals(XSLX)) {
                        array.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    } else if (taskExtension[i].equals(PNG)) {
                        array.add("image/png");
                    } else if (taskExtension[i].equals(JPG)) {
                        array.add("image/jpg");
                    }
                }
                String[] mimeTypes = array.toArray(new String[taskExtension.length]);

                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                startActivityForResult(intent, PICK_IMAGE_GALLERY);
            } else {
                Toast.makeText(this, "Permiso de archivos denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void openImageChooser() {
        final CharSequence[] items = {"Tomar foto", "Seleccionar archivo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(items[0])) {
                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, PICK_IMAGE_CAMERA);
                    }
                } else if (items[item].equals(items[1])) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERY_REQUES_CODE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        String[] taskExtension = ((Task) tasksList.getAdapter().getItem(positionItemSelected)).getExtensionArchivo().split(", ");
                        ArrayList<String> array = new ArrayList<>();

                        for (int i = 0; i < taskExtension.length; i++) {
                            if (taskExtension[i].equals(PDF)) {
                                array.add("application/pdf");
                            } else if (taskExtension[i].equals(XSL)) {
                                array.add("application/vnd.ms-excel");
                            } else if (taskExtension[i].equals(XSLX)) {
                                array.add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                            } else if (taskExtension[i].equals(PNG)) {
                                array.add("image/png");
                            } else if (taskExtension[i].equals(JPG)) {
                                array.add("image/jpg");
                            }
                        }
                        String[] mimeTypes = array.toArray(new String[array.size()]);

                        intent.setType("*/*");
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
                        startActivityForResult(intent, PICK_IMAGE_GALLERY);
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("¿Estas seguro que deseas salir?")
                .setCancelable(false)
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        final ConnectionHTTP connectionHTTP = new ConnectionHTTP(TasksActivity.this);
                        // Ask if is there connection
                        if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                            // Block windows and show the progressbar
                            progressBar.setVisibility(View.VISIBLE);
                            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                            // Call the data stored in preferences
                            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            String token = preferences.getString("token", "");
                            String IdUsuario = preferences.getString("IdUsuario", "");

                            // Send the request to logout
                            connectionHTTP.logout(IdUsuario, token);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_tasks;
    }
}
