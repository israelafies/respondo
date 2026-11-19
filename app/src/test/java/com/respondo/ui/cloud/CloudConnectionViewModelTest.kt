package com.respondo.ui.cloud

import androidx.lifecycle.ViewModelStore
import com.respondo.data.cloud.CloudApiRepository
import com.respondo.data.cloud.CloudApiResult
import com.respondo.data.cloud.ConnectivityMonitor
import com.respondo.data.cloud.DatabaseStatusDto
import com.respondo.data.cloud.HealthDto
import com.respondo.data.cloud.SystemInfoDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CloudConnectionViewModelTest {
    @Test fun offlineDeviceDoesNotCallBackendAndRecoveryChecksActualBackend() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(dispatcher)
        try {
            val monitor = FakeMonitor(false)
            val repository = FakeRepository(CloudApiResult.NetworkFailure(com.respondo.data.cloud.NetworkFailureKind.CONNECTION))
            val viewModel = CloudConnectionViewModel(repository, monitor, configured = true)
            advanceUntilIdle()
            assertEquals(CloudConnectionStatus.OFFLINE, viewModel.state.value.status)
            assertEquals(0, repository.healthCalls)

            monitor.online.value = true
            advanceUntilIdle()
            assertEquals(1, repository.healthCalls)
            assertEquals(CloudConnectionStatus.SERVER_UNAVAILABLE, viewModel.state.value.status)

            monitor.online.value = false
            advanceUntilIdle()
            assertEquals(CloudConnectionStatus.OFFLINE, viewModel.state.value.status)
            assertEquals(1, repository.healthCalls)
            ViewModelStore().apply { put("cloud", viewModel) }.clear()
            assertTrue(monitor.closed)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun configuredHealthyBackendBecomesConnectedAndMissingUrlIsNotConfigured() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val online = FakeMonitor(true)
            val healthy = FakeRepository(CloudApiResult.Success(HealthDto(status = "ok")))
            val viewModel = CloudConnectionViewModel(healthy, online, configured = true)
            advanceUntilIdle()
            assertEquals(CloudConnectionStatus.CONNECTED, viewModel.state.value.status)
            assertEquals(1, healthy.healthCalls)

            val unconfigured = CloudConnectionViewModel(healthy, FakeMonitor(true), configured = false)
            advanceUntilIdle()
            assertEquals(CloudConnectionStatus.NOT_CONFIGURED, unconfigured.state.value.status)
            ViewModelStore().apply { put("healthy", viewModel); put("unconfigured", unconfigured) }.clear()
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test fun manualRetryChecksReachableBackendAgain() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val monitor = FakeMonitor(true)
            val repository = FakeRepository(CloudApiResult.Success(HealthDto(status = "ok")))
            val viewModel = CloudConnectionViewModel(repository, monitor, configured = true)
            advanceUntilIdle()
            viewModel.retry()
            advanceUntilIdle()
            assertEquals(2, repository.healthCalls)
            assertEquals(CloudConnectionStatus.CONNECTED, viewModel.state.value.status)
            ViewModelStore().apply { put("cloud", viewModel) }.clear()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FakeMonitor(initial: Boolean) : ConnectivityMonitor {
        val online = MutableStateFlow(initial)
        var closed = false
        override val hasInternet: StateFlow<Boolean> = online
        override fun close() { closed = true }
    }

    private class FakeRepository(private val result: CloudApiResult<HealthDto>) : CloudApiRepository {
        var healthCalls = 0
        override suspend fun checkBackendHealth(): CloudApiResult<HealthDto> { healthCalls++; return result }
        override suspend fun getSystemInformation(): CloudApiResult<SystemInfoDto> = error("Not expected")
        override suspend fun checkDatabaseConnectivity(): CloudApiResult<DatabaseStatusDto> = error("Not expected")
    }
}
