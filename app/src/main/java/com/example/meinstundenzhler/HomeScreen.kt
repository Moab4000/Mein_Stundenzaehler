@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.meinstundenzhler

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.ui.detail.components.HomeTopBar

@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onListsClick: () -> Unit,
    onAddNoteClick: () -> Unit,
    onMyNotesClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onTipsClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            HomeTopBar(
                onSettingsClick = onSettingsClick,
                onTipsClick = onTipsClick
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
                        "Stundenzähler",
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
                            Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Meine Listen")
                        }
                    }
                }
            }


            //Notizen erstellen
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
                        "Notizen erstellen",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        "Erfasse Notizen schnell und Speichere sie als PDF.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // Primäre Aktionen nebeneinander
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onAddNoteClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Notizen erstellen")
                        }
                        OutlinedButton(
                            onClick = onMyNotesClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = MaterialTheme.shapes.large
                        ) {
                            Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Meine Notizen")
                        }
                    }
                }
            }

            // Rest des Screens bleibt leer/luftig
            Spacer(Modifier.weight(1f))
        }
    }
}
