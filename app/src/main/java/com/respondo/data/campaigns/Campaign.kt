package com.respondo.data.campaigns

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.respondo.data.offers.OfferProfile

enum class CampaignStatus { ACTIVE, PAUSED, COMPLETED }

@Entity(
    tableName = "campaigns",
    foreignKeys = [ForeignKey(
        entity = OfferProfile::class,
        parentColumns = ["id"], childColumns = ["offerProfileId"],
        onDelete = ForeignKey.SET_NULL,
    )],
    indices = [Index("offerProfileId")],
)
data class Campaign(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val objective: String = "",
    val description: String = "",
    val offerProfileId: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: CampaignStatus = CampaignStatus.ACTIVE,
)
