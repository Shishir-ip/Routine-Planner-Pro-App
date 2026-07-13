package com.shishir.routineplannerpro.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.shishir.routineplannerpro.data.AppDatabase
import com.shishir.routineplannerpro.data.RoutineRepository

class RescheduleWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repository = RoutineRepository(AppDatabase.getInstance(applicationContext).routineDao())
        val scheduler = ReminderScheduler(applicationContext)
        repository.allItemsSnapshot().forEach { scheduler.schedule(it) }
        return Result.success()
    }
}
