package com.janad.parrot.presentation.screens.main

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.janad.parrot.data.UserPreferences
import com.janad.parrot.presentation.screens.LoadingScreen
import com.janad.parrot.presentation.screens.auth.LoginScreen
import com.janad.parrot.presentation.screens.auth.RegisterScreen
import com.janad.parrot.presentation.viewmodels.MainViewModel
import kotlinx.coroutines.delay

object Screen {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAIN = "main"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val mainViewModel: MainViewModel = hiltViewModel()

    val context = LocalContext.current
    val tokenFlow = UserPreferences(context).tokenFlow.collectAsState(initial = "")
    var showSplash by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(4000)
        showSplash = false
    }
    if (showSplash) {
        LoadingScreen()
    } else {
        val startDestination =
            if (tokenFlow.value?.isNotEmpty() ?: false) Screen.MAIN else Screen.LOGIN

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.LOGIN) {
                LoginScreen(mainViewModel = mainViewModel, navController = navController)
            }
            composable(Screen.REGISTER) {
                RegisterScreen(mainViewModel = mainViewModel, navController = navController)
            }
            composable(Screen.MAIN) {
           //     ProductsScreen(viewModel = viewModel)
            MainScreen(mainViewModel = mainViewModel)
            }
        }
    }
}
