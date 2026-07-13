package com.shishir.routineplannerpro.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.shishir.routineplannerpro.model.RoutineCategoryEntity
import com.shishir.routineplannerpro.model.RoutineItemEntity

@Database(
    entities = [RoutineCategoryEntity::class, RoutineItemEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun routineDao(): RoutineDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "routine_planner_pro.db"
                ).build().also { instance = it }
            }
        }
    }
}
