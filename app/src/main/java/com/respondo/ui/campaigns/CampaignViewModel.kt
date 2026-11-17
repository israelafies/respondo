package com.respondo.ui.campaigns

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.respondo.data.campaigns.*
import com.respondo.data.prospects.RespondoDatabase
import com.respondo.data.prospects.Prospect
import com.respondo.data.offers.OfferProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CampaignsUiState(val campaigns: List<Campaign> = emptyList(), val prospects: List<Prospect> = emptyList(), val offers: List<OfferProfile> = emptyList(), val error: String? = null)

class CampaignViewModel(application: Application) : AndroidViewModel(application) {
    private val db = RespondoDatabase.get(application)
    private val repository: CampaignRepository = RoomCampaignRepository(db.campaignDao(), db.prospectDao(), db.offerProfileDao())
    val uiState: StateFlow<CampaignsUiState> = kotlinx.coroutines.flow.combine(repository.observeCampaigns(), repository.observeProspects(), repository.observeOfferProfiles()) { campaigns, prospects, offers -> CampaignsUiState(campaigns, prospects, offers) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CampaignsUiState())

    fun members(id: Long) = repository.observeMembers(id)
    fun stats(id: Long) = repository.observeStats(id)
    fun prospect(id: Long) = repository.prospect(id)
    fun save(campaign: Campaign, done: () -> Unit) {
        CampaignValidation.error(campaign.name)?.let { return }
        viewModelScope.launch { runCatching { if (campaign.id == 0L) repository.add(campaign) else repository.update(campaign) }.onSuccess { done() } }
    }
    fun delete(campaign: Campaign, done: () -> Unit) { viewModelScope.launch { runCatching { repository.delete(campaign) }.onSuccess { done() } } }
    fun addProspect(campaignId: Long, prospectId: Long) { viewModelScope.launch { runCatching { repository.addProspect(campaignId, prospectId) } } }
    fun removeProspect(campaignId: Long, prospectId: Long) { viewModelScope.launch { runCatching { repository.removeProspect(campaignId, prospectId) } } }
    fun updateStage(campaignId: Long, prospectId: Long, stage: SalesStage) { viewModelScope.launch { runCatching { repository.updateStage(campaignId, prospectId, stage) } } }
}
