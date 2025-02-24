package com.example.to_dolist;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.to_dolist.Adapter.ToDoAdapter;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class RecyclerViewTouchHelper extends ItemTouchHelper.SimpleCallback {

    // Constants for swipe directions
    private final ToDoAdapter adapter;
    private final Context context;

    public RecyclerViewTouchHelper(ToDoAdapter adapter, Context context) { // Constructor
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
        this.context = context;
    }

    private void showDeleteConfirmationDialog(final int position) { // Show a confirmation dialog before deleting an item
        AlertDialog deleteConfirmationDialog = createDeleteConfirmationDialog(position);
        // If the dialog is not null, show it
        if (deleteConfirmationDialog != null) {
            deleteConfirmationDialog.show();
        }
    }

    private AlertDialog createDeleteConfirmationDialog(final int position) { // Create a confirmation dialog
        // Get the context from the adapter
        Context context = adapter.getContext();
        // If the context is null, return null
        if (context == null) return null;
        // Create and return the confirmation dialog
        return new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.Title_DeleteDialog))
                .setPositiveButton(context.getString(R.string.Yes_DeleteDialog), (dialog, which) -> {
                    adapter.deleteTask(position);
                    dialog.dismiss();
                })
                .setNegativeButton(context.getString(R.string.No_DeleteDialog), (dialog, which) -> {
                    adapter.notifyItemChanged(position);
                    dialog.dismiss();
                })
                .create();
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false; // Not implemented
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) { // Handle swipe events
        // Get the position of the swiped item
        final int position = viewHolder.getAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        // Handle swipe left (edit) and swipe right (delete)
        if (direction == ItemTouchHelper.RIGHT) {
            showDeleteConfirmationDialog(position);
        } else {
            adapter.editTask(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        // Customize the swipe background and icon colors
        new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, R.color.green))
                .addSwipeLeftActionIcon(R.drawable.edit)
                .addSwipeRightBackgroundColor(ContextCompat.getColor(context, R.color.red))
                .addSwipeRightActionIcon(R.drawable.delete)
                .create()
                .decorate();

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }
}
