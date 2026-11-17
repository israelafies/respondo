package com.respondo.ui.offers

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.respondo.data.offers.OfferProfile
import com.respondo.data.offers.OfferProfileRepository
import com.respondo.data.offers.OfferProfileValidation
import com.respondo.data.prospects.RespondoDatabase
import com.respondo.data.offers.RoomOfferProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class OfferProfilesUiState(
    val profiles: List<OfferProfile> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
)

class OfferProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: OfferProfileRepository = RoomOfferProfileRepository(RespondoDatabase.get(application).offerProfileDao())
    private val saving = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<OfferProfilesUiState> = combine(repository.observeProfiles(), saving, error) { profiles, isSaving, message ->
        OfferProfilesUiState(profiles, isSaving, message)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OfferProfilesUiState())

    fun clearError() { error.value = null }

    fun save(profile: OfferProfile, onComplete: () -> Unit) {
        if (saving.value) return
        OfferProfileValidation.error(profile.offerName, profile.providerName)?.let { error.value = it; return }
        viewModelScope.launch {
            saving.value = true
            error.value = null
            try {
                if (profile.id == 0L) repository.add(profile) else repository.update(profile)
                onComplete()
            } catch (_: Exception) {
                error.value = "Could not save this offer profile. Please try again."
            } finally { saving.value = false }
        }
    }

    fun setActive(profile: OfferProfile) {
        viewModelScope.launch {
            runCatching { repository.setActive(profile.id) }
                .onFailure { error.value = "Could not set the active offer. Please try again." }
        }
    }

    fun delete(profile: OfferProfile, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repository.delete(profile) }
                .onSuccess { onComplete() }
                .onFailure { error.value = "Could not delete this offer profile. Please try again." }
        }
    }
}
