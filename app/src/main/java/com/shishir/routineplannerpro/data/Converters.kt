package com.shishir.routineplannerpro.data

import androidx.room.TypeConverter
import com.shishir.routineplannerpro.model.RoutineType

class Converters {
    @TypeConverter
    fun toRoutineType(value: String): RoutineType = RoutineType.valueOf(value)

    @TypeConverter
    fun fromRoutineType(type: RoutineType): String = type.name
}
