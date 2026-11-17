package com.respondo.data.offers

import kotlinx.coroutines.flow.Flow

interface OfferProfileRepository {
    fun observeProfiles(): Flow<List<OfferProfile>>
    suspend fun add(profile: OfferProfile): Long
    suspend fun update(profile: OfferProfile)
    suspend fun delete(profile: OfferProfile)
    suspend fun setActive(id: Long)
}

class RoomOfferProfileRepository(private val dao: OfferProfileDao) : OfferProfileRepository {
    override fun observeProfiles() = dao.observeAll()
    override suspend fun add(profile: OfferProfile) = dao.insert(profile)
    override suspend fun update(profile: OfferProfile) = dao.update(profile)
    override suspend fun delete(profile: OfferProfile) = dao.delete(profile)
    override suspend fun setActive(id: Long) = dao.setActive(id)
}
