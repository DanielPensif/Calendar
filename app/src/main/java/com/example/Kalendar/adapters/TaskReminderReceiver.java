package com.example.Kalendar.adapters;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.RequiresPermission;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.Kalendar.R;

public class TaskReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "task_reminders";

    @Override
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    public void onReceive(Context ctx, Intent intent) {
        String title = intent.getStringExtra("title");
        String text  = intent.getStringExtra("text");
        int requestCode = intent.getIntExtra("requestCode", 0);

        Log.d("TaskReminderReceiver", "onReceive called");
        Log.d("TaskReminderReceiver", "Title: " + title);
        Log.d("TaskReminderReceiver", "Text: " + text);
        Log.d("TaskReminderReceiver", "Request code: " + requestCode);

        createChannel(ctx);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_task)
                .setContentTitle(title != null ? title : "Задача")
                .setContentText(text != null ? text : "Напоминание о задаче")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat.from(ctx).notify(requestCode, builder.build());
    }


    private void createChannel(Context ctx) {
        NotificationManager nm = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о задачах",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(chan);
        }
    }
}
