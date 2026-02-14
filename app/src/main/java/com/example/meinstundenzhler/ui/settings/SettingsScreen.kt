@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.meinstundenzhler.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.settings.SettingsRepo
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repo: SettingsRepo,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val state by repo.flow.collectAsState(initial = null)

    // Sheet-State
    var showThemeSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Einstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { inner ->
        if (state == null) return@Scaffold
        val s = state!!

        // Inhalt
        Column(
            Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Allgemein", style = MaterialTheme.typography.titleMedium)

            // Sektion als Karte
            ElevatedCard(shape = MaterialTheme.shapes.extraLarge) {
                ListItem(
                    headlineContent = { Text("Theme") },
                    supportingContent = { Text("System / Hell / Dunkel") },
                    trailingContent = { Text(displayTheme(s.theme)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeSheet = true }
                        .padding(horizontal = 4.dp)
                )
            }
        }

        // Bottom-Sheet: Theme auswählen
        if (showThemeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showThemeSheet = false },
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Theme wählen", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))

                    ThemeOptionRow(
                        selected = s.theme == "system",
                        icon = { Icon(Icons.Outlined.Smartphone, contentDescription = null) },
                        title = "System",
                        subtitle = "Folgt dem Gerätemodus"
                    ) {
                        scope.launch { repo.setTheme("system") }
                        showThemeSheet = false
                    }
                    ThemeOptionRow(
                        selected = s.theme == "light",
                        icon = { Icon(Icons.Outlined.LightMode, contentDescription = null) },
                        title = "Hell",
                        subtitle = "Helle Farben"
                    ) {
                        scope.launch { repo.setTheme("light") }
                        showThemeSheet = false
                    }
                    ThemeOptionRow(
                        selected = s.theme == "dark",
                        icon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                        title = "Dunkel",
                        subtitle = "Dunkle Farben"
                    ) {
                        scope.launch { repo.setTheme("dark") }
                        showThemeSheet = false
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

/* ---------- Bausteine ---------- */

@Composable
private fun ThemeOptionRow(
    selected: Boolean,
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String,
    onSelect: () -> Unit
) {
    ListItem(
        leadingContent = icon,
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        trailingContent = {
            RadioButton(selected = selected, onClick = onSelect)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    )
}

private fun displayTheme(key: String): String = when (key) {
    "light" -> "Hell"
    "dark" -> "Dunkel"
    else -> "System"
}
