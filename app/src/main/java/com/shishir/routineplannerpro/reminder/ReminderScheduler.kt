package com.shishir.routineplannerpro.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.shishir.routineplannerpro.model.RoutineItemEntity
import com.shishir.routineplannerpro.util.DateTimeUtils
import java.time.ZoneId

class ReminderScheduler(private val context: Context) {
    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(item: RoutineItemEntity) {
        val nextStart = DateTimeUtils.nextOccurrenceStart(item) ?: return

        if (item.reminderEnabled) {
            val triggerAt = nextStart.minusMinutes(item.reminderMinutesBefore.toLong())
            scheduleAlarm(item, "REMINDER", triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }

        if (item.alarmEnabled) {
            val triggerAt = nextStart.minusMinutes(item.alarmMinutesBefore.toLong())
            scheduleAlarm(item, "ALARM", triggerAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
        }
    }

    private fun scheduleAlarm(item: RoutineItemEntity, mode: String, triggerAtMillis: Long) {
        val intent = Intent(context, RoutineAlarmReceiver::class.java).apply {
            putExtra("itemId", item.id)
            putExtra("title", item.title)
            putExtra("startTime", item.startTime)
            putExtra("mode", mode)
        }
        val requestCode = (item.id.toInt() * 100) + if (mode == "ALARM") 2 else 1
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
}
