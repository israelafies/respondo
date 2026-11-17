package com.respondo.data.campaigns

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.respondo.data.prospects.Prospect

enum class SalesStage { NOT_CONTACTED, CONTACTED, INTERESTED, NEGOTIATING, CONVERTED, LOST }

@Entity(
    tableName = "campaign_prospects",
    primaryKeys = ["campaignId", "prospectId"],
    foreignKeys = [
        ForeignKey(entity = Campaign::class, parentColumns = ["id"], childColumns = ["campaignId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Prospect::class, parentColumns = ["id"], childColumns = ["prospectId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("prospectId")],
)
data class CampaignProspect(
    val campaignId: Long,
    val prospectId: Long,
    val stage: SalesStage = SalesStage.NOT_CONTACTED,
    val addedAt: Long = System.currentTimeMillis(),
)
