package com.respondo.data.offers

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offer_profiles")
data class OfferProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val offerName: String,
    val providerName: String,
    val description: String = "",
    val targetCustomers: String = "",
    val problemsSolved: String = "",
    val benefits: String = "",
    val pricing: String = "",
    val differentiators: String = "",
    val objectionsAndResponses: String = "",
    val preferredTone: String = "",
    val aiInstructions: String = "",
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)
