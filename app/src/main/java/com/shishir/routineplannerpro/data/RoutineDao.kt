package com.shishir.routineplannerpro.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.shishir.routineplannerpro.model.RoutineCategoryEntity
import com.shishir.routineplannerpro.model.RoutineItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routine_categories ORDER BY isSystem DESC, name ASC")
    fun getCategories(): Flow<List<RoutineCategoryEntity>>

    @Query("SELECT * FROM routine_categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): RoutineCategoryEntity?

    @Query("SELECT * FROM routine_categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): RoutineCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: RoutineCategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: RoutineCategoryEntity)

    @Query("SELECT * FROM routine_items WHERE categoryId = :categoryId ORDER BY startTime ASC")
    fun getItemsByCategory(categoryId: Long): Flow<List<RoutineItemEntity>>

    @Query("SELECT * FROM routine_items ORDER BY startTime ASC")
    fun getAllItems(): Flow<List<RoutineItemEntity>>

    @Query("SELECT * FROM routine_items")
    suspend fun getAllItemsOnce(): List<RoutineItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: RoutineItemEntity): Long

    @Update
    suspend fun updateItem(item: RoutineItemEntity)

    @Delete
    suspend fun deleteItem(item: RoutineItemEntity)
}
