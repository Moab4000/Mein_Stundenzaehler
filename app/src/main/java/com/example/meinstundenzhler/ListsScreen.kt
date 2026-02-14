@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.meinstundenzhler

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.data.ShiftRepository
import com.example.meinstundenzhler.utils.MONTHS
import com.example.meinstundenzhler.utils.computeDurationMinutes
import com.example.meinstundenzhler.utils.formatHours
import java.text.NumberFormat
import java.util.Locale
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

    val lists by remember(monthlyRepo) {
        monthlyRepo.getAll()
            .map { it.sortedByDescending { m -> m.createdAt } }
    }.collectAsState(initial = emptyList())

    var toDeleteId by remember { mutableStateOf<Long?>(null) }
    var toDeleteTitle by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meine Listen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { inner ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lists, key = { it.id }) { item ->
                // Daten für Karte
                val shifts by shiftRepo.getByMonthlyList(item.id)
                    .collectAsState(initial = emptyList())

                val minutes = remember(shifts) {
                    shifts.sumOf { s -> computeDurationMinutes(s.startEpochMillis, s.endEpochMillis, s.breakMinutes) }
                }
                val earned = minutes / 60.0 * item.hourlyWage
                val totalWithCarry = earned + item.previousDebt
                val carryThisMonth = item.monthlyIncome?.let { totalWithCarry - it }

                ElevatedCard(
                    onClick = { onOpenDetail(item.id) },
                    shape = MaterialTheme.shapes.extraLarge,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {

                        // Kopfzeile
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${MONTHS[item.monthIndex]} ${item.year}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            FilledTonalIconButton(
                                onClick = {
                                    toDeleteId = item.id
                                    toDeleteTitle = "${MONTHS[item.monthIndex]} ${item.year}"
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(8.dp))

                        // Nummernstyles (tabellarisch)
                        val num = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
                        val numStrong = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum")
                        val currency = NumberFormat.getCurrencyInstance(Locale.GERMANY)

                        // Zeilen
                        ValueRow("Arbeitszeit", "${formatHours(minutes)} h", num, numStrong)
                        ValueRow("Verdient (ohne Übertrag)", currency.format(earned), num, numStrong)

                        item.monthlyIncome?.let {
                            ValueRow("Monatlicher Verdienst", currency.format(it), num, numStrong)
                            val carry = carryThisMonth ?: 0.0
                            val color = if (carry < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            ValueRow(
                                "Übertrag (dieser Monat)",
                                (if (carry >= 0) "+" else "−") + currency.format(kotlin.math.abs(carry)),
                                num, numStrong, valueColor = color
                            )
                        } ?: run {
                            ValueRow("Übertrag Vormonat", "%+.2f €".format(item.previousDebt), num, numStrong)
                        }
                    }
                }
            }
        }
    }

    // Löschen-Dialog
    toDeleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { toDeleteId = null; toDeleteTitle = null },
            title = { Text("Eintrag löschen?") },
            text = { Text("Möchtest du „${toDeleteTitle ?: "diesen Monat"}“ wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { monthlyRepo.deleteById(id) }
                    toDeleteId = null; toDeleteTitle = null
                }) { Text("Löschen") }
            },
            dismissButton = {
                TextButton(onClick = { toDeleteId = null; toDeleteTitle = null }) { Text("Abbrechen") }
            }
        )
    }
}

/* ---------- kleine UI-Helfer ---------- */

@Composable
private fun ValueRow(
    label: String,
    value: String,
    baseStyle: androidx.compose.ui.text.TextStyle,
    strongStyle: androidx.compose.ui.text.TextStyle,
    valueColor: androidx.compose.ui.graphics.Color? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = baseStyle, modifier = Modifier.weight(1f))
        Text(
            value,
            style = strongStyle,
            color = valueColor ?: LocalContentColor.current,
            textAlign = TextAlign.End
        )
    }
}
