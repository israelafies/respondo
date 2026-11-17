package com.respondo.data.campaigns

import com.respondo.data.offers.OfferProfile
import com.respondo.data.prospects.Prospect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class CampaignStats(val total: Int, val contacted: Int, val interested: Int, val negotiating: Int, val converted: Int, val lost: Int)

interface CampaignRepository {
    fun observeCampaigns(): Flow<List<Campaign>>
    fun observeMembers(id: Long): Flow<List<CampaignProspect>>
    fun observeStats(id: Long): Flow<CampaignStats>
    fun observeProspects(): Flow<List<Prospect>>
    fun observeOfferProfiles(): Flow<List<OfferProfile>>
    suspend fun add(campaign: Campaign): Long
    suspend fun update(campaign: Campaign)
    suspend fun delete(campaign: Campaign)
    suspend fun addProspect(campaignId: Long, prospectId: Long): Boolean
    suspend fun removeProspect(campaignId: Long, prospectId: Long)
    suspend fun updateStage(campaignId: Long, prospectId: Long, stage: SalesStage)
    suspend fun prospect(id: Long): Prospect?
}

class RoomCampaignRepository(
    private val dao: CampaignDao,
    private val prospectDao: com.respondo.data.prospects.ProspectDao,
    private val offerDao: com.respondo.data.offers.OfferProfileDao,
) : CampaignRepository {
    override fun observeCampaigns() = dao.observeCampaigns()
    override fun observeMembers(id: Long) = dao.observeMembers(id)
    override fun observeStats(id: Long) = combine(dao.observeTotal(id), dao.observeContacted(id), dao.observeInterested(id), dao.observeNegotiating(id), dao.observeConverted(id), dao.observeLost(id)) { values -> CampaignStats(values[0], values[1], values[2], values[3], values[4], values[5]) }
    override fun observeProspects() = prospectDao.observeAll()
    override fun observeOfferProfiles() = offerDao.observeAll()
    override suspend fun add(campaign: Campaign) = dao.insertCampaign(campaign)
    override suspend fun update(campaign: Campaign) = dao.updateCampaign(campaign)
    override suspend fun delete(campaign: Campaign) = dao.deleteCampaign(campaign)
    override suspend fun addProspect(campaignId: Long, prospectId: Long) = dao.addMemberIfAbsent(campaignId, prospectId)
    override suspend fun removeProspect(campaignId: Long, prospectId: Long) = dao.removeMember(campaignId, prospectId)
    override suspend fun updateStage(campaignId: Long, prospectId: Long, stage: SalesStage) = dao.updateStage(campaignId, prospectId, stage)
    override suspend fun prospect(id: Long) = prospectDao.findById(id)
}
