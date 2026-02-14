@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.meinstundenzhler

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.data.Shift
import com.example.meinstundenzhler.data.ShiftRepository
import com.example.meinstundenzhler.ui.detail.components.MonthlySummaryCard
import com.example.meinstundenzhler.ui.detail.components.ShiftItemCard
import com.example.meinstundenzhler.ui.dialogs.EditMonthlyDialog      // <- KORREKTE IMPORTS
import com.example.meinstundenzhler.ui.detail.dialogs.ShiftEditorDialog
import com.example.meinstundenzhler.utils.*
import kotlinx.coroutines.launch
import java.time.ZoneId

@Composable
fun ListDetailScreen(
    listId: Long,
    monthlyRepo: MonthlyListRepository,
    shiftRepo: ShiftRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Shift?>(null) }
    var editing by remember { mutableStateOf<Shift?>(null) }
    var showEditList by remember { mutableStateOf(false) }

    val monthly by monthlyRepo.getById(listId).collectAsState(initial = null)
    val shifts: List<Shift> by shiftRepo.getByMonthlyList(listId).collectAsState(initial = emptyList())

    // Summen (ohne Übertrag)
    val (totalMinutes, totalEurNoCarry) = remember(monthly, shifts) {
        if (monthly == null) 0 to 0.0 else {
            val wage = monthly!!.hourlyWage
            var minutes = 0
            var sum = 0.0
            shifts.forEach { s ->
                val d = computeDurationMinutes(s.startEpochMillis, s.endEpochMillis, s.breakMinutes)
                minutes += d; sum += (d / 60.0) * wage
            }
            minutes to sum
        }
    }
    val income = monthly?.monthlyIncome
    val totalWithCarry = (monthly?.previousDebt ?: 0.0) + totalEurNoCarry
    val currentCarry = income?.let { totalWithCarry - it }

    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val spacerHeight = bottomInset + 96.dp

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(monthly?.let { "${monthName(it.monthIndex)} ${it.year}" } ?: "Monat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Schicht hinzufügen")
            }
        }
    ) { inner ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            monthly?.let { m ->
                MonthlySummaryCard(
                    monthTitle = "${monthName(m.monthIndex)} ${m.year}",
                    hourlyWage = m.hourlyWage,
                    carryOver = m.previousDebt,
                    totalMinutes = totalMinutes.toLong(),
                    totalEarnings = totalEurNoCarry,
                    monthlyEarnings = income,
                    monthCarryOver = currentCarry,
                    onEditClick = { showEditList = true },
                    onPdfClick = {
                        scope.launch {
                            try {
                                val uri = cacheMonthPdf(ctx, listId, monthlyRepo, shiftRepo)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                ctx.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(ctx, "PDF konnte nicht geöffnet werden: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    compact = true,
                    showTitle = false,              // << Titel in der Karte ausblenden
                    modifier = Modifier.fillMaxWidth()
                )

            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(shifts, key = { it.id }) { s: Shift ->
                    ShiftItemCard(
                        shift = s,
                        hourlyWage = monthly?.hourlyWage ?: 0.0,
                        onClick = { editing = s },
                        onDelete = { toDelete = s },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { Spacer(Modifier.height(spacerHeight)) }
            }
        }
    }

    // Dialoge
    if (showEditList && monthly != null) {
        EditMonthlyDialog(
            initial = monthly!!,
            onDismiss = { showEditList = false },
            onSave = { updated -> scope.launch { monthlyRepo.update(updated); showEditList = false } }
        )
    }

    if (showAdd && monthly != null) {
        ShiftEditorDialog(
            title = "Schicht hinzufügen",
            initial = null,
            onDismiss = { showAdd = false },
            onSave = { date, startHm, endHm, breakMin, note ->
                val start = date.atTime(startHm.first, startHm.second)
                var end = date.atTime(endHm.first, endHm.second)
                if (end.isBefore(start)) end = end.plusDays(1)
                val zone = ZoneId.systemDefault()
                val startMs = start.atZone(zone).toInstant().toEpochMilli()
                val endMs = end.atZone(zone).toInstant().toEpochMilli()
                val dur = computeDurationMinutes(startMs, endMs, breakMin)
                if (dur <= 0) {
                    Toast.makeText(ctx, "Dauer ist 0 oder negativ.", Toast.LENGTH_SHORT).show()
                    return@ShiftEditorDialog
                }
                val shift = Shift(
                    monthlyListId = listId,
                    startEpochMillis = startMs,
                    endEpochMillis = endMs,
                    breakMinutes = breakMin,
                    note = note.takeIf { it.isNotBlank() }
                )
                scope.launch { shiftRepo.insert(shift); showAdd = false }
            }
        )
    }

    editing?.let { s ->
        ShiftEditorDialog(
            title = "Schicht bearbeiten",
            initial = s,
            onDismiss = { editing = null },
            onSave = { date, startHm, endHm, breakMin, note ->
                val start = date.atTime(startHm.first, startHm.second)
                var end = date.atTime(endHm.first, endHm.second)
                if (end.isBefore(start)) end = end.plusDays(1)
                val zone = ZoneId.systemDefault()
                val startMs = start.atZone(zone).toInstant().toEpochMilli()
                val endMs = end.atZone(zone).toInstant().toEpochMilli()
                val dur = computeDurationMinutes(startMs, endMs, breakMin)
                if (dur <= 0) {
                    Toast.makeText(ctx, "Dauer ist 0 oder negativ.", Toast.LENGTH_SHORT).show()
                    return@ShiftEditorDialog
                }
                val updated = s.copy(
                    startEpochMillis = startMs,
                    endEpochMillis = endMs,
                    breakMinutes = breakMin,
                    note = note.takeIf { it.isNotBlank() }
                )
                scope.launch { shiftRepo.update(updated); editing = null }
            }
        )
    }

    toDelete?.let { s ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Schicht löschen?") },
            text = { Text("Eintrag wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { shiftRepo.deleteById(s.id) }
                    toDelete = null
                }) { Text("Löschen") }
            },
            dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Abbrechen") } }
        )
    }
}
