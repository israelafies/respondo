package com.respondo.data.prospects

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.respondo.data.offers.OfferProfile
import com.respondo.data.offers.OfferProfileDao
import com.respondo.data.campaigns.Campaign
import com.respondo.data.campaigns.CampaignDao
import com.respondo.data.campaigns.CampaignProspect

@Database(entities = [Prospect::class, OfferProfile::class, Campaign::class, CampaignProspect::class], version = 3, exportSchema = true)
abstract class RespondoDatabase : RoomDatabase() {
    abstract fun prospectDao(): ProspectDao
    abstract fun offerProfileDao(): OfferProfileDao
    abstract fun campaignDao(): CampaignDao

    companion object {
        @Volatile private var instance: RespondoDatabase? = null

        fun get(context: Context): RespondoDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                RespondoDatabase::class.java,
                "respondo.db",
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS `offer_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `offerName` TEXT NOT NULL, `providerName` TEXT NOT NULL, `description` TEXT NOT NULL, `targetCustomers` TEXT NOT NULL, `problemsSolved` TEXT NOT NULL, `benefits` TEXT NOT NULL, `pricing` TEXT NOT NULL, `differentiators` TEXT NOT NULL, `objectionsAndResponses` TEXT NOT NULL, `preferredTone` TEXT NOT NULL, `aiInstructions` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)""")
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `campaigns` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `objective` TEXT NOT NULL, `description` TEXT NOT NULL, `offerProfileId` INTEGER, `createdAt` INTEGER NOT NULL, `status` TEXT NOT NULL, FOREIGN KEY(`offerProfileId`) REFERENCES `offer_profiles`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_campaigns_offerProfileId` ON `campaigns` (`offerProfileId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `campaign_prospects` (`campaignId` INTEGER NOT NULL, `prospectId` INTEGER NOT NULL, `stage` TEXT NOT NULL, `addedAt` INTEGER NOT NULL, PRIMARY KEY(`campaignId`, `prospectId`), FOREIGN KEY(`campaignId`) REFERENCES `campaigns`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`prospectId`) REFERENCES `prospects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_campaign_prospects_prospectId` ON `campaign_prospects` (`prospectId`)")
            }
        }
    }
}
