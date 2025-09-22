package com.janad.parrot.presentation.screens.main

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.janad.parrot.data.ThemePreferences
import com.janad.parrot.data.UserPreferences
import com.janad.parrot.data.models.ui.ThemeSetting
import com.janad.parrot.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Load theme from DataStore
    val themeFlow = remember { ThemePreferences.loadTheme(context) }
    val currentTheme by themeFlow.collectAsState(initial = ThemeSetting.SYSTEM)

    var showThemeDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(text = "Settings", style = MaterialTheme.typography.headlineMedium)

            // Theme selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showThemeDialog = true }
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Theme", style = MaterialTheme.typography.bodyLarge)
                Text(currentTheme.displayName, style = MaterialTheme.typography.bodyMedium)
            }

            // Logout
            Button(
                onClick = { showLogoutDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout", color = MaterialTheme.colorScheme.onError)
            }
        }
    }

    // Theme dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            confirmButton = {},
            title = { Text("Choose Theme") },
            text = {
                Column {
                    ThemeSetting.entries.forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    scope.launch {
                                        ThemePreferences.saveTheme(context, theme)
                                        showThemeDialog = false
                                    }
                                }
                                .padding(8.dp)
                        ) {
                            Text(theme.displayName)
                        }
                    }
                }
            }
        )
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    scope.launch {
                        UserPreferences(context).getRefreshToken()?.let { mainViewModel.logout(it) } // implement in your ViewModel

                    }
                    Toast.makeText(context, "Logged out", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out?") }
        )
    }
}
