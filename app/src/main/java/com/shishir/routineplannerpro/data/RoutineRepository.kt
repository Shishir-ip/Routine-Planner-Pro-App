package com.shishir.routineplannerpro.data

import com.shishir.routineplannerpro.model.AiRoutineBlock
import com.shishir.routineplannerpro.model.ExportCategory
import com.shishir.routineplannerpro.model.ExportItem
import com.shishir.routineplannerpro.model.ExportRoutine
import com.shishir.routineplannerpro.model.RoutineCategoryEntity
import com.shishir.routineplannerpro.model.RoutineItemEntity
import com.shishir.routineplannerpro.model.RoutineType
import kotlinx.coroutines.flow.Flow

class RoutineRepository(private val dao: RoutineDao) {
    val categories: Flow<List<RoutineCategoryEntity>> = dao.getCategories()
    val allItems: Flow<List<RoutineItemEntity>> = dao.getAllItems()

    suspend fun ensureSystemCategories() {
        if (dao.getCategoryByName("Daily Routine") == null) {
            dao.insertCategory(RoutineCategoryEntity(name = "Daily Routine", type = RoutineType.DAILY, isSystem = true))
        }
        if (dao.getCategoryByName("Class Routine") == null) {
            dao.insertCategory(RoutineCategoryEntity(name = "Class Routine", type = RoutineType.CLASS, isSystem = true))
        }
    }

    fun itemsByCategory(categoryId: Long): Flow<List<RoutineItemEntity>> = dao.getItemsByCategory(categoryId)

    suspend fun addCategory(name: String): Long {
        return dao.insertCategory(RoutineCategoryEntity(name = name.trim(), type = RoutineType.CUSTOM, isSystem = false))
    }

    suspend fun removeCategory(category: RoutineCategoryEntity) {
        if (!category.isSystem) dao.deleteCategory(category)
    }

    suspend fun addItem(item: RoutineItemEntity): Long = dao.insertItem(item)

    suspend fun updateItem(item: RoutineItemEntity) = dao.updateItem(item)

    suspend fun deleteItem(item: RoutineItemEntity) = dao.deleteItem(item)

    suspend fun exportCategory(category: RoutineCategoryEntity): ExportRoutine {
        val items = dao.getAllItemsOnce().filter { it.categoryId == category.id }
        return ExportRoutine(
            category = ExportCategory(category.name, category.type.name, category.isSystem),
            items = items.map {
                ExportItem(
                    title = it.title,
                    startTime = it.startTime,
                    endTime = it.endTime,
                    roomNumber = it.roomNumber,
                    classType = it.classType,
                    teacherName = it.teacherName,
                    section = it.section,
                    additionalInfo = it.additionalInfo,
                    daysCsv = it.daysCsv,
                    startDate = it.startDate,
                    endDate = it.endDate,
                    reminderEnabled = it.reminderEnabled,
                    reminderMinutesBefore = it.reminderMinutesBefore,
                    alarmEnabled = it.alarmEnabled,
                    alarmMinutesBefore = it.alarmMinutesBefore
                )
            }
        )
    }

    suspend fun importRoutine(exportRoutine: ExportRoutine) {
        val category = dao.getCategoryByName(exportRoutine.category.name)
            ?: run {
                val type = runCatching { RoutineType.valueOf(exportRoutine.category.type) }.getOrDefault(RoutineType.CUSTOM)
                val newId = dao.insertCategory(
                    RoutineCategoryEntity(
                        name = exportRoutine.category.name,
                        type = type,
                        isSystem = exportRoutine.category.isSystem
                    )
                )
                dao.getCategoryById(newId)
            }
            ?: return

        exportRoutine.items.forEach { item ->
            dao.insertItem(
                RoutineItemEntity(
                    categoryId = category.id,
                    title = item.title,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    roomNumber = item.roomNumber,
                    classType = item.classType,
                    teacherName = item.teacherName,
                    section = item.section,
                    additionalInfo = item.additionalInfo,
                    daysCsv = item.daysCsv,
                    startDate = item.startDate,
                    endDate = item.endDate,
                    reminderEnabled = item.reminderEnabled,
                    reminderMinutesBefore = item.reminderMinutesBefore,
                    alarmEnabled = item.alarmEnabled,
                    alarmMinutesBefore = item.alarmMinutesBefore
                )
            )
        }
    }

    suspend fun importAiBlock(block: AiRoutineBlock) {
        val routineType = runCatching { RoutineType.valueOf(block.routineType.uppercase()) }.getOrDefault(RoutineType.CUSTOM)
        val existing = dao.getCategoryByName(block.routineName)
        val categoryId = existing?.id ?: dao.insertCategory(
            RoutineCategoryEntity(name = block.routineName, type = routineType, isSystem = routineType != RoutineType.CUSTOM)
        )

        block.items.forEach { item ->
            dao.insertItem(
                RoutineItemEntity(
                    categoryId = categoryId,
                    title = item.title,
                    startTime = item.startTime,
                    endTime = item.endTime,
                    roomNumber = item.roomNumber,
                    classType = item.classType,
                    teacherName = item.teacherName,
                    section = item.section,
                    additionalInfo = item.additionalInfo,
                    daysCsv = item.daysCsv,
                    startDate = item.startDate,
                    endDate = item.endDate,
                    reminderEnabled = item.reminderEnabled,
                    reminderMinutesBefore = item.reminderMinutesBefore,
                    alarmEnabled = item.alarmEnabled,
                    alarmMinutesBefore = item.alarmMinutesBefore
                )
            )
        }
    }

    suspend fun allItemsSnapshot(): List<RoutineItemEntity> = dao.getAllItemsOnce()
}
