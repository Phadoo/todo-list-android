package com.example.to_dolist.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.Model.ToDoModel;
import com.example.to_dolist.R;
import com.example.to_dolist.Utils.DatabaseHelper;

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
        holder.textView.setText(item.getTask());
        holder.imageButton.setSelected(toBoolean(item.getStatus()));

        holder.imageButton.setOnClickListener(v -> {
            boolean isChecked = !v.isSelected();
            v.setSelected(isChecked);
            myDB.updateStatus(item.getId(), isChecked ? 1 : 0);
            item.setStatus(isChecked ? 1 : 0);
        });

        holder.textView.setOnClickListener(v -> {
            if (taskClickListener != null) {
                taskClickListener.onTaskClicked(position, item);
            }
        });
    }

    public void editTask(int position) { // Handle edit task click
        if (position >= 0 && position < mList.size() && taskClickListener != null) {
            ToDoModel task = mList.get(position);
            taskClickListener.onTaskClicked(position, task); // Delegate to listener
        }
    }

    public boolean toBoolean(int num) {
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

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageButton = itemView.findViewById(R.id.imageButton);
            textView = itemView.findViewById(R.id.imageButton_text);
        }
    }
}
