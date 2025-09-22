package com.janad.parrot

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.janad.parrot.data.ThemePreferences
import com.janad.parrot.data.models.ui.ThemeSetting
import com.janad.parrot.presentation.components.NetworkBanner
import com.janad.parrot.ui.theme.ParrotTheme
import com.janad.parrot.presentation.screens.main.AppNavHost
import com.janad.parrot.utils.NetworkMonitor
import com.janad.parrot.utils.RequestAllPermissions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NetworkMonitor.startMonitoring(this)
        enableEdgeToEdge()
        setContent {
            RequestAllPermissions {
                granted ->
                if (!granted) {
                    Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()

                }

            }
            val isConnected by NetworkMonitor::isConnected
            var darkTheme by remember { mutableStateOf(true) }
            val context = this
            val themeFlow = ThemePreferences.loadTheme(context)
            val currentTheme by themeFlow.collectAsState(initial = ThemeSetting.SYSTEM)
            ParrotTheme(currentTheme== ThemeSetting.DARK) {


                Box(modifier = Modifier.fillMaxSize()) {
                    AppNavHost()

                    // Network banner overlay
                    NetworkBanner(isConnected = isConnected)
                }
            }
        }

    }
}

