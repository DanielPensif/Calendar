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

public class EventReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "event_reminders";

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    public void onReceive(Context ctx, Intent intent) {
        // Логирование полученных данных
        String title = intent.getStringExtra("title");
        String text  = intent.getStringExtra("text");
        int requestCode = intent.getIntExtra("requestCode", 0);

        Log.d("EventReminderReceiver", "Received reminder for event: " + title);
        Log.d("EventReminderReceiver", "Text: " + text);
        Log.d("EventReminderReceiver", "RequestCode: " + requestCode);

        createChannel(ctx);

        // Создание уведомления
        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_event)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        // Отправка уведомления
        NotificationManagerCompat.from(ctx).notify(requestCode, builder.build());
    }

    private void createChannel(Context ctx) {
        NotificationManager nm = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID,
                    "Напоминания о событиях",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(chan);
        }
    }
}
