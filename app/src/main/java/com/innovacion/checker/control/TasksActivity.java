package com.innovacion.checker.control;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.innovacion.checker.R;
import com.innovacion.checker.model.Task;
import com.innovacion.checker.model.Territorie;
import com.innovacion.checker.utils.ConnectionHTTP;
import com.innovacion.checker.utils.FileChooserActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Date;
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
    private String[] mimeTypes;

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
     * Initialize UI variables. <br>
     * <b>post: </b> Variables are initialized. <br>
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tasksList = findViewById(R.id.tasksList);
        searchBar = findViewById(R.id.searchBar);
        searchBtn = findViewById(R.id.searchBtn);
        swiperefresh = findViewById(R.id.swiperefresh);
        progressBar = findViewById(R.id.progressBar);


        // Set listener to the search bar
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

        //Set titles of the top bar
        TextView projectName = findViewById(R.id.titleOne);
        TextView territorieName = findViewById(R.id.titleTwo);

        Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
        projectName.setText(territorie != null ? territorie.getProjectName() : null);
        territorieName.setText(territorie != null ? territorie.getTerritorieName() : null);


        // Set action to profile image icon to display the popup to logout
        findViewById(R.id.optionsMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        //Initialize variables related with layout filter
        final ImageView button_filter = findViewById(R.id.button_filter);
        final ConstraintLayout filterLayout = findViewById(R.id.filter_layout);
        filterLayout.setVisibility(View.INVISIBLE);
        final CheckBox filter_not_reported = filterLayout.findViewById(R.id.filter_not_reported);
        final CheckBox filter_reported = filterLayout.findViewById(R.id.filter_reported);
        final CheckBox filter_approved = filterLayout.findViewById(R.id.filter_approved);
        final CheckBox filter_not_approved = filterLayout.findViewById(R.id.filter_not_approved);
        Button filter_clear = filterLayout.findViewById(R.id.filter_clear);

        //Set listener to show filter layout when the button is clicked
        button_filter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!filterLayout.isShown())
                    filterLayout.setVisibility(View.VISIBLE);
                else
                    filterLayout.setVisibility(View.INVISIBLE);
            }
        });

        //Set listener to refresh tasks swiping
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

        //Set listener to clean filters
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

        //Set listener to filter results
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
                        } else if (filter_reported.isChecked() && taskStatus.equals("1")) {
                            tasksFiltered.add(task);
                        } else if (filter_approved.isChecked() && taskStatus.equals("2")) {
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

        //Set on item click listener to show and load the correct dialog and info, according to the task type
        tasksList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                filterLayout.setVisibility(View.INVISIBLE);
                final Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
                final Task task = (Task) tasksList.getAdapter().getItem(position);
                if (task.getTaskType() == 1) {
                    positionItemSelected = position;
                    deliverableDialog = new DeliverableDialog(TasksActivity.this, task, territorie, "", "");
                    deliverableDialog.setCancelable(true);
                    deliverableDialog.show();

                    //Set listener to the attach file button
                    Button attachFileBtn = deliverableDialog.findViewById(R.id.attachFileBtn);
                    attachFileBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openImageChooser();
                        }
                    });

                    //Set listener to the send report button
                    Button sendReportBtn = deliverableDialog.findViewById(R.id.sendReportBtn);
                    sendReportBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String base64 = deliverableDialog.getBase64();
                            String nameFile = deliverableDialog.getNameFile();

                            if (base64 != null && !base64.isEmpty()) {
                                final ConnectionHTTP connectionHTTP = new ConnectionHTTP(TasksActivity.this);
                                if (connectionHTTP.isNetworkAvailable(getApplicationContext())) {
                                    progressBar.setVisibility(View.VISIBLE);
                                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                    String token = preferences.getString("token", "");

                                    connectionHTTP.setAttachTask(territorie.getProjectID(), territorie.getTerritorieID(), task.getTaskID(), token, base64, ((EditText) deliverableDialog.findViewById(R.id.commentTxt)).getText().toString(), nameFile);
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

    /**
     * Returns true if the searched text is contained in the destination text . <br>
     *
     * @param str       The destination. v != null && v != "".
     * @param searchStr The searched text (introduced by user). v != null && v != "".
     */

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

    /**
     * Load the list of tasks which each name contains the content of the search bar text. <br>
     * <b>pre: </b> The list is loaded with all project/territory tasks. <br>
     * <b>post: </b> The list contains only the tasks that contains the search characters in their names<br>
     */

    final public void searchByName() {
        ArrayList<Task> tasksFiltered = new ArrayList<>();
        String taskName = "";
        for (int k = 0; tasks != null && k < tasks.size(); k++) {
            Task task = tasks.get(k);
            if (containsIgnoreCase(task.getTaskName(), searchBar.getText().toString())) {
                tasksFiltered.add(task);
            }
        }
        TaskAdapter taskAdapter = new TaskAdapter(TasksActivity.this, tasksFiltered);
        tasksList.setAdapter(taskAdapter);
    }

    /**
     * Hide the soft keyboard <br>
     * <b>post: </b> The soft keyboard is hide<br>
     */

    final public void hideSoftKeyBoard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Send to server the user request to get tasks. <br>
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
            String idUser = preferences.getString("IdUsuario", "");

            connectionHTTP.getTasks(idUser, idProject, idTerritore, code, token);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.failed_connection), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Initialize and assign action to the options menu. <br>
     * <b>pre: </b> Show popup to send to server the logout request. <br>
     * <b>post: </b> The user session is closed. <br>
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
     * Receive the response of logout, attach and get projects request from server<br>
     * <b>pre: </b> progressBar != null. <br>
     *
     * @param result  Response of logout, attach and get projects request from server. result != null && result != "".
     * @param service Service requested to server. service != null && service != "".
     */

    @Override
    public void onResultReceived(String result, String service) {
        if (service.equals(ConnectionHTTP.ATTACH_TASK)) {
            try {
                JSONObject respuesta = new JSONObject(result);
                boolean exito = respuesta.getBoolean("exito");

                if (exito) {
                    Toast.makeText(TasksActivity.this, respuesta.getString("message"), Toast.LENGTH_LONG).show();
                    refreshList();
                } else {
                    Toast.makeText(TasksActivity.this, respuesta.getString("message"), Toast.LENGTH_LONG).show();
                }

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

                        tasks.add(new Task(taskID, taskType, processID, process, subprocess, taskName, status, expirationDate, extensionArchivo, task));
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
                //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                if (exito) {
                    Intent intent = new Intent(TasksActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
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
     * Receive the image captured by camera. <br>
     *
     * @param requestCode Request code from activity. requestCode != null && requestCode != "".
     * @param resultCode  Result code from activity sended to server. resultCode != null && resultCode != "".
     * @param data        Data sent from activity. data != null && data != "".
     */

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == RESULT_OK) {
            String encodedBase64 = "";
            if (requestCode == PICK_IMAGE_CAMERA) {
                encodedBase64 = sendImageCaptured(data);

                String filename = "CAM-"+(new Date()).toString()+".png";
                deliverableDialog.setNameFile(filename);
            } else if (requestCode == PICK_IMAGE_GALLERY) {
                encodedBase64 = sendFileSelected(data);
            }

            final Territorie territorie = (Territorie) getIntent().getSerializableExtra("territorie");
            final Task task = (Task) tasksList.getAdapter().getItem(positionItemSelected);

            if (task.getTaskType() == 1) {
                deliverableDialog.setBase64(encodedBase64);
                Button b = deliverableDialog.findViewById(R.id.attachFileBtn);
                if(!encodedBase64.equals("")) b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_vector_attach_icon, 0, 0, 0);
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

    /**
     * Encode the image captured by camera. <br>
     * <b>post: </b> The file is encoded <br>
     *
     * @param data The image captured. data != null && data != "".
     */

    private String sendImageCaptured(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap imageBitmap = (Bitmap) (extras != null ? extras.get("data") : null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Objects.requireNonNull(imageBitmap).compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String base64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return base64;
    }

    /**
     * Encode the image selected from gallery. <br>
     * <b>post: </b> The file is encoded <br>
     *
     * @param data The image selected. data != null && data != "".
     */

    private String sendFileSelected(Intent data) {
        String base64 = "";

        // Get the Uri of the selected file
        Uri uri = data.getData();
        String uriString = uri.toString();
        File myFile = new File(uriString);
        String path = myFile.getAbsolutePath();
        String displayName = "";

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }

        String getPath = uri.getPath();

        String mime = getMimeType(getPath);
        boolean mim = false;
        for (int i = 0; mime!=null && i < mimeTypes.length && !mim; i++) {
            if (mime.equals(mimeTypes[i])) {
                mim = true;
            }
        }

        if (!mim) {
            Toast.makeText(TasksActivity.this, "No se permite este tipo de archivos", Toast.LENGTH_LONG).show();
        } else {
        deliverableDialog.setNameFile(displayName);
        try {
            File file = new File(getPath);

            if ((file.length() / (1024 * 1024)) < 20) {
                byte[] buffer = new byte[(int) file.length() + 100];
                @SuppressWarnings("resource")
                int length = new FileInputStream(file).read(buffer);
                base64 = Base64.encodeToString(buffer, 0, length,
                        Base64.DEFAULT);
            } else {
                base64 = "";
                Toast.makeText(TasksActivity.this, "Se ha superado el tamaño maximo", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            base64 = "";
        }
    }

        return base64;
    }

    // url = file path or whatever suitable URL you want.
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Receive the permissions required by camera. <br>
     *
     * @param requestCode  Request code from activity. requestCode != null && requestCode != "".
     * @param permissions  Permissions that are required by camera. permissions != null && permissions != "".
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

                mimeTypes = array.toArray(new String[taskExtension.length]);
                startActivityForResult(new Intent(TasksActivity.this, FileChooserActivity.class), PICK_IMAGE_GALLERY);
            } else {
                Toast.makeText(this, "Permiso de archivos denegado", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Open the image chooser. <br>
     * <b>post: </b> The image chooser is opened  <br>
     */

    public void openImageChooser() {
        final CharSequence[] items = {"Tomar foto", "Seleccionar archivo"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(items[0])) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                        } else {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent, PICK_IMAGE_CAMERA);
                        }
                    }else{
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, PICK_IMAGE_CAMERA);
                    }
                } else if (items[item].equals(items[1])) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERY_REQUES_CODE);
                        } else {
                            verifyStoragePermissions();

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
                            mimeTypes = array.toArray(new String[array.size()]);

                            startActivityForResult(new Intent(TasksActivity.this, FileChooserActivity.class), PICK_IMAGE_GALLERY);
                        }
                    }else{

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
                        mimeTypes = array.toArray(new String[array.size()]);

                        startActivityForResult(new Intent(TasksActivity.this, FileChooserActivity.class), PICK_IMAGE_GALLERY);
                    }
                }
            }
        });
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_GALLERY_REQUES_CODE);
        }
    }

    @Override
    int getContentViewId() {
        return R.layout.activity_tasks;
    }
}
