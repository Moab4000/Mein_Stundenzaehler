package com.example.meinstundenzhler

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
import com.example.meinstundenzhler.data.ShiftRepository
import com.example.meinstundenzhler.utils.computeDurationMinutes
import com.example.meinstundenzhler.utils.formatHours
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun ListsScreen(
    onBack: () -> Unit,
    monthlyRepo: MonthlyListRepository,
    shiftRepo: ShiftRepository,
    onOpenDetail: (Long) -> Unit
) {
    val scope = rememberCoroutineScope()

    val monthsNames = listOf(
        "Januar","Februar","März","April","Mai","Juni",
        "Juli","August","September","Oktober","November","Dezember"
    )

    val lists by monthlyRepo.getAll()
        .map { it.sortedByDescending { m -> m.createdAt } }
        .collectAsState(initial = emptyList())

    // Platz am Ende der Liste
    val bottomInset  = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val spacerHeight = bottomInset + 96.dp

    var toDeleteId by remember { mutableStateOf<Long?>(null) }
    var toDeleteTitle by remember { mutableStateOf<String?>(null) }

    Box(Modifier.fillMaxSize().padding(16.dp)) {

        Column(
            modifier = Modifier.align(Alignment.TopCenter).statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Meine Listen", style = MaterialTheme.typography.titleLarge)
        }

        LazyColumn(
            modifier = Modifier.align(Alignment.Center).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(lists, key = { it.id }) { item ->
                // Summen für die Karte berechnen
                val shifts by shiftRepo.getByMonthlyList(item.id).collectAsState(initial = emptyList())
                val minutes = remember(shifts) {
                    shifts.sumOf { s -> computeDurationMinutes(s.startEpochMillis, s.endEpochMillis, s.breakMinutes) }
                }
                val earned = minutes / 60.0 * item.hourlyWage
                val totalWithCarry = earned + item.previousDebt
                val carryThisMonth = item.monthlyIncome?.let { totalWithCarry - it }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(item.id) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("${monthsNames[item.monthIndex]} ${item.year}",
                                style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(6.dp))
                            Text("Arbeitszeit: ${formatHours(minutes)} h")
                            Text("Verdient (ohne Übertrag): ${"%.2f".format(earned)} €")
                            item.monthlyIncome?.let {
                                Text("Monatlicher Verdienst: ${"%.2f".format(it)} €")
                                Text("Übertrag (dieser Monat): %+.2f €".format(carryThisMonth ?: 0.0))
                            } ?: Text("Übertrag Vormonat: %+.2f €".format(item.previousDebt))
                        }
                        IconButton(
                            onClick = {
                                toDeleteId = item.id
                                toDeleteTitle = "${monthsNames[item.monthIndex]} ${item.year}"
                            }
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Löschen", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // 👇 Unsichtbarer Spacer am Ende
            item { Spacer(Modifier.height(spacerHeight)) }
        }

        // Zurück (unten links)
        FilledTonalIconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.BottomStart).navigationBarsPadding().size(56.dp)
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück") }
    }

    // Bestätigung löschen
    toDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { toDeleteId = null; toDeleteTitle = null },
            title = { Text("Eintrag löschen?") },
            text  = { Text("Möchtest du „${toDeleteTitle ?: "diesen Monat"}“ wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { monthlyRepo.deleteById(id) }
                    toDeleteId = null; toDeleteTitle = null
                }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { toDeleteId = null; toDeleteTitle = null }) { Text("Abbrechen") } }
        )
    }
}
