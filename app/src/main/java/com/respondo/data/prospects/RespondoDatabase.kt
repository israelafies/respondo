package com.respondo.data.prospects

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.respondo.data.offers.OfferProfile
import com.respondo.data.offers.OfferProfileDao

@Database(entities = [Prospect::class, OfferProfile::class], version = 2, exportSchema = true)
abstract class RespondoDatabase : RoomDatabase() {
    abstract fun prospectDao(): ProspectDao
    abstract fun offerProfileDao(): OfferProfileDao

    companion object {
        @Volatile private var instance: RespondoDatabase? = null

        fun get(context: Context): RespondoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RespondoDatabase::class.java,
                "respondo.db",
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `offer_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `offerName` TEXT NOT NULL, `providerName` TEXT NOT NULL, `description` TEXT NOT NULL, `targetCustomers` TEXT NOT NULL, `problemsSolved` TEXT NOT NULL, `benefits` TEXT NOT NULL, `pricing` TEXT NOT NULL, `differentiators` TEXT NOT NULL, `objectionsAndResponses` TEXT NOT NULL, `preferredTone` TEXT NOT NULL, `aiInstructions` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)""")
            }
        }
    }
}
