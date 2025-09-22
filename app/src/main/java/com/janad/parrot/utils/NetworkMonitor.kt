package com.janad.parrot.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Monitors network connectivity and provides a reactive state for UI updates.
 */
object NetworkMonitor {

    var isConnected by mutableStateOf(true)
        private set

    fun startMonitoring(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected = true
                showToast(context, "Back online")
            }

            override fun onLost(network: Network) {
                isConnected = false
                showToast(context, "No internet connection")
            }
        })
    }

    private fun showToast(context: Context, message: String) {
        // Use application context to allow showing toast from anywhere
        Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT).show()
    }
}
