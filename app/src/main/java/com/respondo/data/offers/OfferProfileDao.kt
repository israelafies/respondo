package com.respondo.data.offers

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OfferProfileDao {
    @Query("SELECT * FROM offer_profiles ORDER BY isActive DESC, updatedAt DESC, id DESC")
    fun observeAll(): Flow<List<OfferProfile>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(profile: OfferProfile): Long

    @Update
    suspend fun update(profile: OfferProfile)

    @Delete
    suspend fun delete(profile: OfferProfile)

    @Query("UPDATE offer_profiles SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE offer_profiles SET isActive = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun activate(id: Long, updatedAt: Long): Int

    @Transaction
    suspend fun setActive(id: Long) {
        clearActive()
        activate(id, System.currentTimeMillis())
    }
}
