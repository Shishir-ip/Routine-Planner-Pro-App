package com.shishir.routineplannerpro

import android.content.Context
import com.shishir.routineplannerpro.data.AppDatabase
import com.shishir.routineplannerpro.data.RoutineRepository
import com.shishir.routineplannerpro.network.OpenRouterService
import com.shishir.routineplannerpro.reminder.ReminderScheduler
import com.shishir.routineplannerpro.settings.SettingsRepository

class AppContainer(context: Context) {
    private val db = AppDatabase.getInstance(context)
    val repository = RoutineRepository(db.routineDao())
    val settingsRepository = SettingsRepository(context)
    val openRouterService = OpenRouterService()
    val scheduler = ReminderScheduler(context)
}
