package com.example.to_dolist;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.Adapter.ToDoAdapter;
import com.example.to_dolist.Model.ToDoModel;
import com.example.to_dolist.Utils.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListener, ToDoAdapter.TaskClickListener {

    RecyclerView recyclerView; // RecyclerView to display tasks
    FloatingActionButton addButton; // FloatingActionButton to add new tasks
    DatabaseHelper myDB; // DatabaseHelper to interact with the database
    private List<ToDoModel> mList; // List to store tasks
    private ToDoAdapter adapter; // Bridge between data (mList) and the UI (recyclerView)

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

        // Initialize components
        recyclerView = findViewById(R.id.recyclerView);
        addButton = findViewById(R.id.addButton);
        myDB = new DatabaseHelper(MainActivity.this);
        mList = new ArrayList<>();
        adapter = new ToDoAdapter(myDB, this ,this);

        // Set up RecyclerView
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Fetch tasks from the database and update the adapter
        mList = myDB.getAllTasks();
        adapter.setTasks(mList);

        // Set up the FloatingActionButton to open the AddNewTask dialog
        addButton.setOnClickListener(v -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));

        // Set up swipe-to-delete functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(adapter, this));
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onTaskClicked(int position, ToDoModel task) { // Callback method from ToDoAdapter when a task is clicked
        // Handle task edit
        Bundle bundle = new Bundle();
        bundle.putInt(AddNewTask.ID_KEY, task.getId());
        bundle.putString(AddNewTask.TASK_KEY, task.getTask());

        // Create and show the AddNewTask dialog (edit task)
        AddNewTask editTaskDialog = AddNewTask.newInstance();
        editTaskDialog.setArguments(bundle);
        editTaskDialog.show(getSupportFragmentManager(), AddNewTask.TAG);
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) { // Callback method from AddNewTask when the dialog is closed
        mList = myDB.getAllTasks();
        adapter.setTasks(mList);
    }
}