package com.respondo.ui.prospects

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.respondo.data.prospects.Prospect
import com.respondo.data.prospects.ProspectRepository
import com.respondo.data.prospects.ProspectValidation
import com.respondo.data.prospects.RespondoDatabase
import com.respondo.data.prospects.RoomProspectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProspectsUiState(
    val prospects: List<Prospect> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
    val isSaving: Boolean = false,
    val error: String? = null,
)

class ProspectViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ProspectRepository = RoomProspectRepository(RespondoDatabase.get(application).prospectDao())
    private val query = MutableStateFlow("")
    private val isSaving = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProspectsUiState> = combine(
        repository.observeProspects(), query, isSaving, error,
    ) { prospects, search, saving, message ->
        ProspectsUiState(
            prospects = prospects.filter { prospect ->
                search.isBlank() || listOf(prospect.fullName, prospect.businessName, prospect.phoneNumber)
                    .any { it.contains(search.trim(), ignoreCase = true) }
            },
            totalCount = prospects.size,
            query = search,
            isSaving = saving,
            error = message,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProspectsUiState())

    fun search(value: String) { query.value = value }
    fun clearError() { error.value = null }

    fun save(prospect: Prospect, onComplete: () -> Unit) {
        if (isSaving.value) return // Protect against rapid repeated taps.
        ProspectValidation.error(prospect.fullName, prospect.businessName, prospect.phoneNumber)?.let {
            error.value = it
            return
        }
        viewModelScope.launch {
            isSaving.value = true
            error.value = null
            try {
                if (prospect.id == 0L) repository.add(prospect) else repository.update(prospect)
                onComplete()
            } catch (_: Exception) {
                error.value = "Could not save this prospect. Please try again."
            } finally {
                isSaving.value = false
            }
        }
    }

    fun delete(prospect: Prospect, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            runCatching { repository.delete(prospect) }
                .onSuccess { onComplete() }
                .onFailure { error.value = "Could not delete this prospect. Please try again." }
        }
    }
}
