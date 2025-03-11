package com.example.to_dolist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.to_dolist.Utils.NotificationHelper;

public class DeadlineReceiver extends BroadcastReceiver {

    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_NAME = "extra_task_name";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DeadlineReceiver", "onReceive called");

        // Check for notification permission (required for Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("DeadlineReceiver", "No notification permission!"); // Could be handled better
                return;
            }
        }

        // Extract task details from the intent
        int taskId = intent.getIntExtra(EXTRA_TASK_ID, -1);
        String taskName = intent.getStringExtra(EXTRA_TASK_NAME);

        if (taskId != -1 && taskName != null) {
            // Show notification
            NotificationHelper helper = new NotificationHelper(context);
            helper.showNotification("Task Deadline Passed", "Deadline for task '" + taskName + "' has passed.", taskId);
            // TODO: Update this
            //new DatabaseHelper(context).updateStatus(taskId, 2);  // Example: Status 2 = Overdue
        } else {
            Log.e("DeadlineReceiver", "Invalid intent data!");
        }
    }
}
