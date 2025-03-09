package com.example.to_dolist;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.to_dolist.Adapter.ToDoAdapter;
import com.example.to_dolist.Model.ToDoModel;
import com.example.to_dolist.Utils.DatabaseHelper;
import com.example.to_dolist.Utils.NotificationHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener, ToDoAdapter.TaskClickListener, AddNewTask.OnTaskAddedListener {

    // Initialize components
    RecyclerView recyclerView; // RecyclerView to display tasks
    FloatingActionButton addButton; // FloatingActionButton to add new tasks
    DatabaseHelper myDB; // DatabaseHelper to interact with the database
    private List<ToDoModel> mList; // List to store tasks
    private ToDoAdapter adapter; // Bridge between data (mList) and the UI (recyclerView)
    private TextView emptyTextView; // TextView to display a message when the list is empty
    private SwipeRefreshLayout swipeRefreshLayout; // SwipeRefreshLayout for refreshing the list

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emptyTextView = findViewById(R.id.emptyTextView);
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        myDB = new DatabaseHelper(MainActivity.this);
        mList = new ArrayList<>();
        adapter = new ToDoAdapter(myDB, this ,this);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        Button notifyButton = findViewById(R.id.notifyButton);
        notifyButton.setOnClickListener(v -> {
            // Check notification permission for Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 100);
                    return;
                }
            }

            // Show notification
            NotificationHelper helper = new NotificationHelper(this);
            helper.showNotification(
                    "New Message",
                    "You have received a new message!",
                    1
            );
        });

        /*

        TODO: Check if spinner is still needed as a feature
        Files included:
        - spinner_item.xml
        - spinner_dropdown_item.xml
        - styles.xml

        AutoCompleteTextView spinnerTextView = findViewById(R.id.categorySpinner);
        String[] items = {"All", "Due today", "A day before", "Less than a week"};
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, items) {
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                // Customize dropdown item appearance
                //textView.setPadding(20, 16, 20, 16);
                return view;
            }
        };
        categoryAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerTextView.setAdapter(categoryAdapter);

        */

        // Set up RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Fetch tasks from the database and update the adapter
        mList = myDB.getAllTasks();
        updateUI();
        adapter.setTasks(mList);

        // Set up the FloatingActionButton to open the AddNewTask dialog
        addButton.setOnClickListener(v -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));

        // Set up swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(adapter, this));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Set up swipe-to-refresh functionality and color scheme
        swipeRefreshLayout.setColorSchemeResources(R.color.light_blue);
        swipeRefreshLayout.setOnRefreshListener(this::refreshTasks);
    }

    @Override
    public void onTaskAdded() {
        adapter.setTasks(mList); // Update the adapter with the new task addition
    }

    private void updateUI() { // Update the UI based on the list of tasks
        if (mList.isEmpty()) {
            emptyTextView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyTextView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void refreshTasks() {
        new Handler().postDelayed(() -> { // Simulate a delay for refreshing
            mList = myDB.getAllTasks();

            // Call database checker
            checkDatabase(mList);

            adapter.setTasks(mList);
            updateUI();
            swipeRefreshLayout.setRefreshing(false); // Stop refreshing
        }, 2000); // Delay in milliseconds
    }

    public void checkDatabase(List<ToDoModel> mList) { // For checking
        // Log all database contents
        Log.d("DatabaseContents", "--- All Tasks in Database ---");
        for (ToDoModel task : mList) {
            // Convert long timestamp to readable date
            //String dateTimeStr = "N/A";
            long dateTime = task.getDateTime();
            Date date = new Date(dateTime);
            String dateTimeStr = String.valueOf(dateTime);
//            if (dateTime > 0) {
//                SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault());
//                //Date date = new Date(dateTime);
//                //dateTimeStr = sdf.format(date);
//                dateTimeStr = String.valueOf(dateTime);
//            }

            Log.d("DatabaseContents", "ID: " + task.getId() +
                    ", Task: " + task.getTask() +
                    ", Status: " + (task.getStatus() == 1 ? "Completed" : "Active") +
                    ", DateTime: " + dateTimeStr +
                    ", Position: " + task.getPosition());
        }
        Log.d("DatabaseContents", "--- End of Database Contents ---");
    }

    @Override
    public void onTaskClicked(int position, ToDoModel task) { // Callback method from ToDoAdapter when a task is clicked
        // Handle task edit
        Bundle bundle = new Bundle();
        bundle.putInt(AddNewTask.ID_KEY, task.getId());
        bundle.putString(AddNewTask.TASK_KEY, task.getTask());
        bundle.putLong(AddNewTask.DATE_TIME_KEY, task.getDateTime());

        // Create and show the AddNewTask dialog (edit task)
        AddNewTask editTaskDialog = AddNewTask.newInstance();
        editTaskDialog.setArguments(bundle);
        editTaskDialog.show(getSupportFragmentManager(), AddNewTask.TAG);
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) { // Callback method from AddNewTask when the dialog is closed
        mList = myDB.getAllTasks();
        adapter.setTasks(mList);
        updateUI();
    }
}