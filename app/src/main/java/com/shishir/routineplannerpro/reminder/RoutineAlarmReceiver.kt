package com.shishir.routineplannerpro.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.shishir.routineplannerpro.MainActivity
import com.shishir.routineplannerpro.R

class RoutineAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title").orEmpty()
        val time = intent.getStringExtra("startTime").orEmpty()
        val mode = intent.getStringExtra("mode") ?: "REMINDER"

        createChannels(context)

        if (mode == "ALARM") {
            val alarmIntent = Intent(context, AlarmAlertActivity::class.java).apply {
                putExtra("title", title)
                putExtra("time", time)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(alarmIntent)
        }

        val launchIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            1000,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (mode == "ALARM") "routine_alarm" else "routine_reminder"
        val text = if (mode == "ALARM") "Alarm time reached for $title" else "$title starts at $time"

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Routine Planner Pro - $mode")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, mode == "ALARM")
            .build()

        NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
    }

    private fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)

        val reminder = NotificationChannel("routine_reminder", "Routine Reminders", NotificationManager.IMPORTANCE_HIGH)
        val alarm = NotificationChannel("routine_alarm", "Routine Alarms", NotificationManager.IMPORTANCE_HIGH)

        manager.createNotificationChannel(reminder)
        manager.createNotificationChannel(alarm)
    }
}
