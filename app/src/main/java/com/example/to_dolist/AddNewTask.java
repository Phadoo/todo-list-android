package com.example.to_dolist;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.to_dolist.Model.ToDoModel;
import com.example.to_dolist.Utils.DatabaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class AddNewTask extends BottomSheetDialogFragment {

    // Constants for passing data between fragments
    public static final String TAG = "AddNewTask";
    public static final String TASK_KEY = "task";
    public static final String ID_KEY = "Id";

    // Member variables
    private DatabaseHelper myDB;
    private EditText mEditText;

    public static AddNewTask newInstance(int taskId, String task) { // Factory method to create a new instance of the fragment
        Bundle args = new Bundle();
        args.putInt(ID_KEY, taskId);
        args.putString(TASK_KEY, task);
        AddNewTask fragment = new AddNewTask();
        fragment.setArguments(args);
        return fragment;
    }

    public static AddNewTask newInstance() { // Factory method to create a new instance of the fragment
        return new AddNewTask();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.add_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) { // Handle UI interactions
        super.onViewCreated(view, savedInstanceState);

        // Initialize components
        mEditText = view.findViewById(R.id.edit_text);
        Button mSaveButton = view.findViewById(R.id.addButton);
        Button mCancelButton = view.findViewById(R.id.cancelButton);

        // Use MainActivity's DatabaseHelper instance (passed via constructor/setter if needed)
        myDB = new DatabaseHelper(requireActivity());

        // Check if this is an update or a new task
        boolean isUpdate = false;
        int taskId = -1;

        // If arguments are passed, this is an update - otherwise, it's a new task; Bundles are used to pass data between fragments
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(ID_KEY)) {
            isUpdate = true;
            taskId = bundle.getInt(ID_KEY);
            String task = bundle.getString(TASK_KEY, "");
            mEditText.setText(task);
        }

        // Initialize Save Button State
        updateSaveButtonState(mEditText.getText().toString().isEmpty());

        // Request focus and show keyboard
        showKeyboard();

        // Listen for text changes in mEditText
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // Update Save Button State
                updateSaveButtonState(s.toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Capture values in final var
        final boolean finalIsUpdate = isUpdate;
        final int finalTaskId = taskId;

        // Set up click listeners
        mSaveButton.setOnClickListener(v -> handleSave(finalTaskId, finalIsUpdate));
        mCancelButton.setOnClickListener(v -> dismiss());
    }

    private void updateSaveButtonState(boolean isEmpty) { // Update Save Button State
        Button saveButton = requireView().findViewById(R.id.addButton);
        saveButton.setEnabled(!isEmpty);
        saveButton.setTextColor(isEmpty ? Color.GRAY : ContextCompat.getColor(requireContext(), R.color.light_blue));
    }

    private void showKeyboard() {
        mEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE); // InputMethodManager is used to show the keyboard
        imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private void handleSave(int taskId, boolean isUpdate) { // Handle Save Button Click
        String text = mEditText.getText().toString();
        if (isUpdate) {
            myDB.updateTask(taskId, text);
        } else {
            ToDoModel item = new ToDoModel();
            item.setTask(text);
            item.setStatus(0);
            myDB.insertTask(item);
        }
        dismiss();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) { // Handles the dismissal of the dialog. Clears the keyboard and notifies the listener about the dialog being closed.
        super.onDismiss(dialog);
        hideKeyboard();
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener) {
            ((OnDialogCloseListener) activity).onDialogClose(dialog);
        }
    }
}
