package com.respondo.data.prospects

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Stable local identity and timestamps leave room for later sync metadata. */
@Entity(tableName = "prospects")
data class Prospect(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val businessName: String,
    val phoneNumber: String,
    val businessDescription: String,
    val problemOrWeakness: String,
    val howICanHelp: String,
    val serviceOrOffer: String,
    val additionalNotes: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
