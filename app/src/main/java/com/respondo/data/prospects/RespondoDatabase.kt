package com.respondo.data.prospects

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Prospect::class], version = 1, exportSchema = true)
abstract class RespondoDatabase : RoomDatabase() {
    abstract fun prospectDao(): ProspectDao

    companion object {
        @Volatile private var instance: RespondoDatabase? = null

        fun get(context: Context): RespondoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RespondoDatabase::class.java,
                "respondo.db",
            ).build().also { instance = it }
        }
    }
}
