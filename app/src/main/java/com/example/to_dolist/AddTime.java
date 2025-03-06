package com.example.to_dolist;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;
import java.util.List;

public class AddTime extends BottomSheetDialogFragment {

    // Constants for passing data between fragments
    public static final String TAG = "AddTime";

    public static AddTime newInstance() { return new AddTime(); }

    // For Data Callback (Interface)
    public interface OnDateTimeSetListener {
        void onDateTimeSet(long dateTime);
    }

    private OnDateTimeSetListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);

        dialog.setOnShowListener(dialogInterface -> {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialogInterface;
            View bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(layoutParams);

                BottomSheetBehavior<View> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false); // Optional: prevent dragging
            }
        });

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_time, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button cancelTimeButton = view.findViewById(R.id.cancelTimeButton);
        Button addTimeButton = view.findViewById(R.id.addTimeButton);
        Button timeTabButton = view.findViewById(R.id.timeTabButton);
        Button dateTabButton = view.findViewById(R.id.dateTabButton);
        DatePicker datePicker = view.findViewById(R.id.datePicker);
        TimePicker timePicker = view.findViewById(R.id.timePicker);

        List<String> months = List.of("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

        updateDateTab(dateTabButton, datePicker, months);
        updateTimeTab(timeTabButton, timePicker);

        dateTabButton.setSelected(true);
        dateTabButton.requestFocus();

        timeTabButton.setOnClickListener(v -> {
            timeTabButton.setSelected(true);
            timeTabButton.requestFocus();
            dateTabButton.setSelected(false);
            datePicker.setVisibility(View.GONE);
            timePicker.setVisibility(View.VISIBLE);
        });

        dateTabButton.setOnClickListener(v -> {
            dateTabButton.setSelected(true);
            dateTabButton.requestFocus();
            timeTabButton.setSelected(false);
            datePicker.setVisibility(View.VISIBLE);
            timePicker.setVisibility(View.GONE);
        });

        datePicker.setOnDateChangedListener((view1, year, monthOfYear, dayOfMonth) -> {
            updateDateTab(dateTabButton, datePicker, months);
        });

        timePicker.setOnTimeChangedListener((view1, hourOfDay, minute) -> {
            updateTimeTab(timeTabButton, timePicker);
        });

        addTimeButton.setOnClickListener(v -> {
            int year = datePicker.getYear();
            int month = datePicker.getMonth();
            int day = datePicker.getDayOfMonth();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // Use Calendar to create a timestamp
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, hour, minute);
            long dateTime = calendar.getTimeInMillis();

            if (listener != null) {
                listener.onDateTimeSet(dateTime);
            }

            dismiss();
        });

        cancelTimeButton.setOnClickListener(v -> dismiss());
    }

    private void updateDateTab(Button dateTabButton, DatePicker datePicker, List<String> months) {
        int year = datePicker.getYear();
        int month = datePicker.getMonth(); // 0-based (0 = January)
        int day = datePicker.getDayOfMonth();
        dateTabButton.setText(day + " " + months.get(month) + " " + year);
    }

    private void updateTimeTab(Button timeTabButton, TimePicker timePicker) {
        int hour = timePicker.getHour(); // 24-hour format
        int minute = timePicker.getMinute();
        String time = String.format("%02d:%02d", hour, minute);
        timeTabButton.setText(time);
    }

    public void setListener(OnDateTimeSetListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
    }
}
