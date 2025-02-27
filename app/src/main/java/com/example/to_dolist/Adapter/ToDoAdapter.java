package com.example.to_dolist.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.Model.ToDoModel;
import com.example.to_dolist.R;
import com.example.to_dolist.Utils.DatabaseHelper;

import java.util.Collections;
import java.util.List;

public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.MyViewHolder> {

    private List<ToDoModel> mList;
    private final DatabaseHelper myDB;
    private final TaskClickListener taskClickListener;
    private final Context context;

    public interface TaskClickListener { // Interface for handling task clicks
        void onTaskClicked(int position, ToDoModel task);
    }

    public ToDoAdapter(DatabaseHelper myDB, Context context, TaskClickListener listener) {
        this.myDB = myDB;
        this.context = context;
        this.taskClickListener = listener;
    }

    public Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) { // Inflate the layout for each item in the RecyclerView
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_layout, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) { // Bind data to the views in each item
        ToDoModel item = mList.get(position);
        holder.textView.setText(item.getTask()); // Set the task text
        holder.imageButton.setSelected(toBoolean(item.getStatus()));

        holder.cardView.setOnClickListener(v -> { // Click listener for the cardView
            if (taskClickListener != null) {
                taskClickListener.onTaskClicked(position, item);
            }
        });

        holder.cardView.setOnLongClickListener(v -> { // Long click listener for the cardView
            if (taskClickListener != null) { // If a listener is set
                Toast.makeText(context, "Long Click detected", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    public void onItemMove(int fromPosition, int toPosition) { // Handle item move
        if (fromPosition < toPosition) { // If fromPosition is less than toPosition
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(mList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(mList, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        myDB.updateIndices(mList); // Update the indices in the database
    }

    public void toggleTaskStatus(int position) { // Toggle the status of a task
        if (position >= 0 && position < mList.size()) { // Check if the position is valid
            // Get the task at the given position
            ToDoModel task = mList.get(position);
            // Calculate the new status (toggle between 0 and 1)
            int newStatus = task.getStatus() == 0 ? 1 : 0;

            updateTaskStatus(task.getId(), newStatus, position); // Call updateTaskStatus with the new status

            task.setStatus(newStatus); // Update the task's status in the database

            if (newStatus == 1) {
                // Remove from current position
                mList.remove(position);
                // Add to end of list
                mList.add(task);
                // Notify adapter of the move
                notifyItemRemoved(position);
                notifyItemInserted(mList.size() - 1);
            } else {
                // Remove from current position
                mList.remove(position);
                // Add to end of list
                mList.add(0, task);
                // Notify adapter of the move
                notifyItemRemoved(position);
                notifyItemInserted(0);
            }
            myDB.updateIndices(mList);
        }
    }

    private void updateTaskStatus(int id, int status, int position) { // Update the status of a task in the database and in the mList
        myDB.updateStatus(id, status); // ID
        mList.get(position).setStatus(status); // Status
        notifyItemChanged(position);
    }

    public void editTask(int position) { // Handle edit task click
        if (position >= 0 && position < mList.size() && taskClickListener != null) {
            ToDoModel task = mList.get(position);
            taskClickListener.onTaskClicked(position, task); // Delegate to listener
        }
    }

    public boolean toBoolean(int num) { // Convert an integer to a boolean
        return num != 0;
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0; // Return the number of items in the mList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<ToDoModel> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    public void deleteTask(int position) {
        ToDoModel item = mList.get(position);
        myDB.deleteTask(item.getId());
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageButton imageButton;
        TextView textView;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton);
            textView = itemView.findViewById(R.id.imageButton_text);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }
}
