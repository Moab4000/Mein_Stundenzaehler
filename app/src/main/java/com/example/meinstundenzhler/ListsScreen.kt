package com.example.meinstundenzhler

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.MonthlyListRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListsScreen(
    onBack: () -> Unit,
    repository: MonthlyListRepository,
    onOpenDetail: (Long) -> Unit
) {
    val months = listOf(
        "Januar","Februar","März","April","Mai","Juni",
        "Juli","August","September","Oktober","November","Dezember"
    )

    val lists by repository.getAll()
        .map { it.sortedByDescending { e -> e.createdAt } }
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    // Zustand für Bestätigungsdialog
    var toDeleteId by remember { mutableStateOf<Long?>(null) }
    var toDeleteTitle by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))
            AppTitle(text = "Meine Listen")
        }

        LazyColumn(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = lists, key = { it.id }) { item ->

                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value != SwipeToDismissBoxValue.Settled) {
                            toDeleteId = item.id
                            toDeleteTitle = "${months[item.monthIndex]} ${item.year}"
                            false // nicht sofort entfernen; erst bestätigen
                        } else {
                            false
                        }
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = true,
                    enableDismissFromEndToStart = true,
                    backgroundContent = {
                        val isSwiping = dismissState.targetValue != SwipeToDismissBoxValue.Settled
                        val bg = if (isSwiping)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(bg)
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDetail(item.id) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = "${months[item.monthIndex]} ${item.year}",
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Text("Stundenlohn: ${"%.2f".format(item.hourlyWage)} €/h")
                                Text(
                                    "Monatlicher Verdienst: " +
                                            (item.monthlyIncome?.let { "%.2f".format(it) + " €" } ?: "—")
                                )
                                Text("Übertrag Vormonat: %+.2f €".format(item.previousDebt))

                            }

                            IconButton(
                                onClick = {
                                    toDeleteId = item.id
                                    toDeleteTitle = "${months[item.monthIndex]} ${item.year}"
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Eintrag löschen",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        // Zurück unten links
        FilledTonalIconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(56.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Zurück"
            )
        }
    }

    // Bestätigungs-Dialog
    if (toDeleteId != null) {
        AlertDialog(
            onDismissRequest = { toDeleteId = null; toDeleteTitle = null },
            title = { Text("Eintrag löschen?") },
            text = {
                Text(
                    toDeleteTitle?.let { "Möchtest du „$it“ wirklich löschen?" }
                        ?: "Möchtest du den Eintrag wirklich löschen?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = toDeleteId ?: return@TextButton
                        scope.launch {
                            repository.deleteById(id)
                            toDeleteId = null
                            toDeleteTitle = null
                        }
                    }
                ) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { toDeleteId = null; toDeleteTitle = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}
