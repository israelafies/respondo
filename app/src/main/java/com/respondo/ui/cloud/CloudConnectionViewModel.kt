package com.respondo.ui.cloud

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.respondo.data.cloud.CloudApiRepository
import com.respondo.data.cloud.CloudApiResult
import com.respondo.data.cloud.ConnectivityMonitor
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class CloudConnectionStatus { NOT_CONFIGURED, OFFLINE, CONNECTING, CONNECTED, SERVER_UNAVAILABLE }

data class CloudConnectionState(val status: CloudConnectionStatus = CloudConnectionStatus.CONNECTING)

class CloudConnectionViewModel(
    private val repository: CloudApiRepository,
    private val connectivity: ConnectivityMonitor,
    private val configured: Boolean,
) : ViewModel() {
    private val mutableState = MutableStateFlow(CloudConnectionState())
    val state: StateFlow<CloudConnectionState> = mutableState.asStateFlow()
    private var healthJob: Job? = null

    init {
        viewModelScope.launch {
            connectivity.hasInternet.distinctUntilChanged().collectLatest { online ->
                if (!configured) {
                    mutableState.value = CloudConnectionState(CloudConnectionStatus.NOT_CONFIGURED)
                } else if (!online) {
                    healthJob?.cancel()
                    mutableState.value = CloudConnectionState(CloudConnectionStatus.OFFLINE)
                } else {
                    checkBackend()
                }
            }
        }
    }

    fun retry() {
        if (!configured) {
            mutableState.value = CloudConnectionState(CloudConnectionStatus.NOT_CONFIGURED)
        } else if (!connectivity.hasInternet.value) {
            mutableState.value = CloudConnectionState(CloudConnectionStatus.OFFLINE)
        } else {
            healthJob?.cancel()
            healthJob = viewModelScope.launch { checkBackend() }
        }
    }

    private suspend fun checkBackend() {
        if (!configured) return
        mutableState.value = CloudConnectionState(CloudConnectionStatus.CONNECTING)
        val result = repository.checkBackendHealth()
        mutableState.value = CloudConnectionState(
            if (result is CloudApiResult.Success && result.value.status == "ok") {
                CloudConnectionStatus.CONNECTED
            } else {
                CloudConnectionStatus.SERVER_UNAVAILABLE
            },
        )
    }

    override fun onCleared() {
        connectivity.close()
        super.onCleared()
    }
}
