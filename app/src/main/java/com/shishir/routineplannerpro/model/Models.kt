package com.shishir.routineplannerpro.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class RoutineType {
    DAILY,
    CLASS,
    CUSTOM
}

@Entity(tableName = "routine_categories")
data class RoutineCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: RoutineType,
    val isSystem: Boolean = false
)

@Entity(
    tableName = "routine_items",
    foreignKeys = [
        ForeignKey(
            entity = RoutineCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class RoutineItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val title: String,
    val startTime: String,
    val endTime: String,
    val roomNumber: String = "",
    val classType: String = "",
    val teacherName: String = "",
    val section: String = "",
    val additionalInfo: String = "",
    val daysCsv: String = "EVERYDAY",
    val startDate: String? = null,
    val endDate: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 5,
    val alarmEnabled: Boolean = false,
    val alarmMinutesBefore: Int = 5,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class ExportRoutine(
    val category: ExportCategory,
    val items: List<ExportItem>
)

@Serializable
data class ExportCategory(
    val name: String,
    val type: String,
    val isSystem: Boolean
)

@Serializable
data class ExportItem(
    val title: String,
    val startTime: String,
    val endTime: String,
    val roomNumber: String = "",
    val classType: String = "",
    val teacherName: String = "",
    val section: String = "",
    val additionalInfo: String = "",
    val daysCsv: String = "EVERYDAY",
    val startDate: String? = null,
    val endDate: String? = null,
    val reminderEnabled: Boolean = false,
    val reminderMinutesBefore: Int = 5,
    val alarmEnabled: Boolean = false,
    val alarmMinutesBefore: Int = 5
)

@Serializable
data class AiRoutineOutput(
    val routines: List<AiRoutineBlock> = emptyList()
)

@Serializable
data class AiRoutineBlock(
    val routineName: String,
    val routineType: String,
    val items: List<ExportItem>
)
