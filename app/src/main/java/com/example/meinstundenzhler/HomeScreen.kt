@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.meinstundenzhler

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onListsClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mein Stundenzähler") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Einstellungen")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero-Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Willkommen 👋",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Erfasse Schichten schnell und erstelle Abrechnungen als PDF.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Primäre Aktionen nebeneinander
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onStartClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Liste erstellen")
                        }
                        OutlinedButton(
                            onClick = onListsClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Outlined.ViewList, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Meine Listen")
                        }
                    }
                }
            }

            // kleine Info-Zeile (optional)
            Surface(
                tonalElevation = 2.dp,
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Tipps",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        "• Pausen werden berücksichtigt\n" +
                                "• Summen pro Monat automatisch\n" +
                                "• PDF wird direkt geöffnet",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Rest des Screens bleibt leer/luftig
            Spacer(Modifier.weight(1f))
        }
    }
}
