package com.example.meinstundenzhler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meinstundenzhler.data.AppDatabase
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.data.ShiftRepository
import com.example.meinstundenzhler.settings.SettingsRepo
import com.example.meinstundenzhler.ui.theme.MeinStundenzählerTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (android.os.Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.Theme_MeinStundenzähler)
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val ctx = LocalContext.current
            val settingsRepo = remember(ctx) { SettingsRepo(ctx) }
            val themePref by settingsRepo.flow.map { it.theme }.collectAsState(initial = "system")

            // Compose-Theme direkt aus der Einstellung ableiten
            val useDark = when (themePref) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MeinStundenzählerTheme(useDarkTheme = useDark) {
                // <-- HIER: Systemleisten anpassen (muss in der Komposition sein)
                val systemUi = rememberSystemUiController()
                val statusBarColor = MaterialTheme.colorScheme.surface
                SideEffect {
                    systemUi.setStatusBarColor(statusBarColor, darkIcons = !useDark)
                    systemUi.setNavigationBarColor(
                        Color.Transparent,
                        darkIcons = !useDark,
                        navigationBarContrastEnforced = false
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav(settingsRepo)
                }
            }
        }
    }
}

@Composable
fun AppNav(settingsRepo: SettingsRepo) {
    val nav = rememberNavController()
    val ctx = LocalContext.current

    val db = remember(ctx) { AppDatabase.getInstance(ctx) }
    val monthlyRepo = remember { MonthlyListRepository(db.monthlyListDao()) }
    val shiftRepo = remember { ShiftRepository(db.shiftDao()) }

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartClick = { nav.navigate("start") },
                onListsClick = { nav.navigate("lists") },
                onSettingsClick = { nav.navigate("settings") }
            )
        }
        composable("start") {
            StartScreen(
                onBack = { nav.popBackStack() },
                repository = monthlyRepo,
                onSaved = { nav.navigate("lists") }
            )
        }
        composable("lists") {
            ListsScreen(
                onBack = { nav.popBackStack() },
                monthlyRepo = monthlyRepo,
                shiftRepo = shiftRepo,
                onOpenDetail = { id -> nav.navigate("detail/$id") }
            )
        }
        composable("detail/{id}") { backStack ->
            val id = backStack.arguments?.getString("id")!!.toLong()
            ListDetailScreen(
                listId = id,
                monthlyRepo = monthlyRepo,
                shiftRepo = shiftRepo,
                onBack = { nav.popBackStack() }
            )
        }
        composable("settings") {
            com.example.meinstundenzhler.ui.settings.SettingsScreen(
                repo = settingsRepo,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
