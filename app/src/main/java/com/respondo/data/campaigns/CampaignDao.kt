package com.respondo.data.campaigns

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC, id DESC") fun observeCampaigns(): Flow<List<Campaign>>
    @Query("SELECT * FROM campaign_prospects WHERE campaignId = :campaignId ORDER BY addedAt DESC") fun observeMembers(campaignId: Long): Flow<List<CampaignProspect>>
    @Query("SELECT COUNT(*) FROM campaign_prospects WHERE campaignId = :campaignId") fun observeTotal(campaignId: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM campaign_prospects WHERE campaignId = :campaignId AND stage IN ('CONTACTED','INTERESTED','NEGOTIATING','CONVERTED','LOST')") fun observeContacted(campaignId: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM campaign_prospects WHERE campaignId = :campaignId AND stage = 'INTERESTED'") fun observeInterested(campaignId: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM campaign_prospects WHERE campaignId = :campaignId AND stage = 'NEGOTIATING'") fun observeNegotiating(campaignId: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM campaign_prospects WHERE campaignId = :campaignId AND stage = 'CONVERTED'") fun observeConverted(campaignId: Long): Flow<Int>
    @Query("SELECT COUNT(*) FROM campaign_prospects WHERE campaignId = :campaignId AND stage = 'LOST'") fun observeLost(campaignId: Long): Flow<Int>
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertCampaign(campaign: Campaign): Long
    @Update suspend fun updateCampaign(campaign: Campaign)
    @Delete suspend fun deleteCampaign(campaign: Campaign)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertMember(member: CampaignProspect): Long
    @Query("DELETE FROM campaign_prospects WHERE campaignId = :campaignId AND prospectId = :prospectId") suspend fun removeMember(campaignId: Long, prospectId: Long)
    @Query("UPDATE campaign_prospects SET stage = :stage WHERE campaignId = :campaignId AND prospectId = :prospectId") suspend fun updateStage(campaignId: Long, prospectId: Long, stage: SalesStage)
    @Query("SELECT * FROM campaign_prospects WHERE campaignId = :campaignId AND prospectId = :prospectId LIMIT 1") suspend fun findMember(campaignId: Long, prospectId: Long): CampaignProspect?
    @Transaction suspend fun addMemberIfAbsent(campaignId: Long, prospectId: Long): Boolean = insertMember(CampaignProspect(campaignId, prospectId)) != -1L
}
