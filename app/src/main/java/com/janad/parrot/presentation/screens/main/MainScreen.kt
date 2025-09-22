package com.janad.parrot.presentation.screens.main
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import com.janad.parrot.R
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.janad.parrot.presentation.viewmodels.MainViewModel
import androidx.core.net.toUri

// Bottom navigation items (without Create)
sealed class BottomNavItem(val title: String, val route: String, val icon: ImageVector) {
    object Home : BottomNavItem("Home", "home", Icons.Default.Home)
    object Contacts : BottomNavItem("Contacts", "contacts", Icons.Default.Contacts)
    object Settings : BottomNavItem("Settings", "settings", Icons.Default.Settings)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(mainViewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()

    // List of bottom nav items
    val bottomItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Contacts,
        BottomNavItem.Settings
    )

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create")
            }

        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            NavigationBar {
                val currentRoute = currentRoute(navController)
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = { navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        } },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(BottomNavItem.Home.route) { ProductsScreen(mainViewModel = mainViewModel) }
            composable(BottomNavItem.Contacts.route) { ContactsScreen() }
            composable(BottomNavItem.Settings.route) { SettingsScreen(mainViewModel) }
            composable("create") { CreateProductScreen(mainViewModel) }
        }
    }
}

// Helper to get current route
@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}



@Preview
@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val phoneNumber = stringResource(id = R.string.contact_phone)
    val telegramUsername = stringResource(id = R.string.contact_telegram)
    val whatsappPhoneNumber = stringResource(id = R.string.contact_whatsapp)

    fun openTelegram(username: String) {
        val telegramIntent = Intent(Intent.ACTION_VIEW).apply {
            setPackage("org.telegram.messenger.web") // explicit package
            data = "https://t.me/$username".toUri() // Telegram will handle this internally
        }

        try {
            context.startActivity(telegramIntent)
        } catch (_: ActivityNotFoundException) {
            // fallback to browser
            val webIntent = Intent(Intent.ACTION_VIEW, "https://t.me/$username".toUri())
            context.startActivity(webIntent)
        }
    }

    @SuppressLint("QueryPermissionsNeeded", "UseKtx")
    fun openWhatsApp(phoneNumber: String) {
        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
            data = "whatsapp://send?phone=$phoneNumber".toUri()
        }
        // Check if WhatsApp is installed
        if (whatsappIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(whatsappIntent)
        } else {
            // Fallback to web link
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$phoneNumber"))
            context.startActivity(webIntent)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF6a11cb), Color(0xFF2575fc))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "Contact Us",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6a11cb),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Telegram button
            Button(
                onClick = { openTelegram(telegramUsername) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0088cc))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_telegram),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(27.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Telegram", fontSize = 18.sp, color = Color.White)
            }

            // WhatsApp button
            Button(
                onClick = { openWhatsApp(whatsappPhoneNumber) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_whatsapp),
                    tint = Color.Unspecified,
                    contentDescription = null,
                    modifier = Modifier.size(27.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("WhatsApp", fontSize = 18.sp, color = Color.White)
            }

            // Phone button
            Button(
                onClick = {
                    val phoneNumber = phoneNumber
                    val intent = Intent(Intent.ACTION_DIAL, "tel:$phoneNumber".toUri())
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6a11cb))
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(27.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("Call Us", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}




