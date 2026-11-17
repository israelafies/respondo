package com.respondo.data.prospects

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProspectDao {
    @Query("SELECT * FROM prospects ORDER BY updatedAt DESC, id DESC")
    fun observeAll(): Flow<List<Prospect>>

    @Query("SELECT * FROM prospects WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Prospect?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(prospect: Prospect): Long

    @Update
    suspend fun update(prospect: Prospect)

    @Delete
    suspend fun delete(prospect: Prospect)
}
