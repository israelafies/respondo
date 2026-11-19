package com.respondo.data.cloud

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.StateFlow

interface ConnectivityMonitor : AutoCloseable {
    val hasInternet: StateFlow<Boolean>
    override fun close()
}

class AndroidConnectivityMonitor(context: Context) : ConnectivityMonitor {
    private val manager = context.applicationContext.getSystemService(ConnectivityManager::class.java)
    private val mutableHasInternet = kotlinx.coroutines.flow.MutableStateFlow(readConnectivity())
    override val hasInternet: StateFlow<Boolean> = mutableHasInternet

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refresh()
        override fun onLost(network: Network) = refresh()
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) = refresh()
    }

    init {
        manager.registerNetworkCallback(NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), callback)
    }

    private fun refresh() { mutableHasInternet.value = readConnectivity() }

    private fun readConnectivity(): Boolean {
        val network = manager.activeNetwork ?: return false
        val capabilities = manager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    override fun close() {
        runCatching { manager.unregisterNetworkCallback(callback) }
    }
}
