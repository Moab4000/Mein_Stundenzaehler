package com.example.meinstundenzhler

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.meinstundenzhler.data.AppDatabase
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.ui.theme.MeinStundenzählerTheme
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import com.example.meinstundenzhler.data.ShiftRepository


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MeinStundenzählerTheme {
                androidx.compose.material3.Surface(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    AppNav()
                }
            }
        }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val db = AppDatabase.getInstance(ctx)
    val monthlyRepo = MonthlyListRepository(db.monthlyListDao())
    val shiftRepo = ShiftRepository(db.shiftDao())

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onStartClick = { nav.navigate("start") },
                onListsClick  = { nav.navigate("lists") }
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
                shiftRepo = shiftRepo,                      // ⬅️ neu
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
    }
}


