package com.respondo.data.prospects

import kotlinx.coroutines.flow.Flow

/** Repository boundary keeps local storage replaceable by a synchronized implementation later. */
interface ProspectRepository {
    fun observeProspects(): Flow<List<Prospect>>
    suspend fun getProspect(id: Long): Prospect?
    suspend fun add(prospect: Prospect): Long
    suspend fun update(prospect: Prospect)
    suspend fun delete(prospect: Prospect)
}

class RoomProspectRepository(private val dao: ProspectDao) : ProspectRepository {
    override fun observeProspects() = dao.observeAll()
    override suspend fun getProspect(id: Long) = dao.findById(id)
    override suspend fun add(prospect: Prospect) = dao.insert(prospect)
    override suspend fun update(prospect: Prospect) = dao.update(prospect)
    override suspend fun delete(prospect: Prospect) = dao.delete(prospect)
}
