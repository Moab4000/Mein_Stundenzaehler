package com.example.meinstundenzhler

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.data.Shift
import com.example.meinstundenzhler.data.ShiftRepository
import com.example.meinstundenzhler.ui.detail.dialogs.EditMonthlyDialog
import com.example.meinstundenzhler.ui.detail.dialogs.ShiftEditorDialog
import com.example.meinstundenzhler.utils.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListDetailScreen(
    listId: Long,
    monthlyRepo: MonthlyListRepository,
    shiftRepo: ShiftRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    val monthly by monthlyRepo.getById(listId).collectAsState(initial = null)
    val shifts by shiftRepo.getByMonthlyList(listId).collectAsState(initial = emptyList())

    // Summen
    val (totalMinutes, totalEur) = remember(monthly, shifts) {
        if (monthly == null) 0 to 0.0 else {
            val wage = monthly!!.hourlyWage
            var minutes = 0
            var sum = 0.0
            shifts.forEach { s ->
                val dur = computeDurationMinutes(s.startEpochMillis, s.endEpochMillis, s.breakMinutes)
                minutes += dur
                sum += (dur / 60.0) * wage
            }
            minutes to sum + monthly!!.previousDebt
        }
    }
    val income = monthly?.monthlyIncome
    val currentCarry = income?.let { totalEur - it }

    // ---------- PDF: direkt öffnen (Cache + ACTION_VIEW) ----------
    fun openPdfPreview() {
        scope.launch {
            try {
                val uri = cacheMonthPdf(
                    context = ctx,
                    listId = listId,
                    monthlyRepo = monthlyRepo,
                    shiftRepo = shiftRepo
                )
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                ctx.startActivity(Intent.createChooser(intent, "PDF öffnen"))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(ctx, "Keine PDF-App gefunden.", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(ctx, "PDF konnte nicht geöffnet werden: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Shift?>(null) }
    var editing by remember { mutableStateOf<Shift?>(null) }
    var showEditList by remember { mutableStateOf(false) }

    // Abstand unten, damit FABs keine Karten verdecken
    val bottomInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val spacerHeight = bottomInset + 96.dp

    // ---------- Root: Box (für FAB-Overlay) ----------
    Box(modifier = Modifier.fillMaxSize()) {

        // Inhalt: Header oben, Liste darunter
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .statusBarsPadding()
        ) {

            // Header-Zeile
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Titel + Infos zentriert
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = monthly?.let { monthName(it.monthIndex) + " " + it.year } ?: "Lade…",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = monthly?.let {
                            "Lohn: %.2f €/h | Übertrag: %+.2f €".format(it.hourlyWage, it.previousDebt)
                        } ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Summe: %s h | %.2f €".format(formatHours(totalMinutes), totalEur),
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (income != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Monatlicher Verdienst: %.2f €".format(income),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Übertrag (dieser Monat): %+.2f €".format(currentCarry!!),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                // Edit + PDF rechts
                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = {
                        if (monthly != null) showEditList = true
                        else Toast.makeText(ctx, "Liste lädt…", Toast.LENGTH_SHORT).show()
                    }) { Icon(Icons.Filled.Edit, contentDescription = "Liste bearbeiten") }

                    IconButton(onClick = { openPdfPreview() }) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "PDF anzeigen")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Liste
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(shifts, key = { it.id }) { s ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editing = s },
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                val (dateStr, rangeStr, durStr, eurStr) =
                                    presentShift(s, monthly?.hourlyWage ?: 0.0)
                                Text(dateStr, style = MaterialTheme.typography.labelLarge)
                                Text(rangeStr, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("$durStr  |  $eurStr", style = MaterialTheme.typography.bodyMedium)
                                if ((s.note ?: "").isNotBlank()) {
                                    Text("Notiz: ${s.note}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            IconButton(onClick = { toDelete = s }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Löschen",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                // unsichtbarer Abstand am Ende
                item { Spacer(Modifier.height(spacerHeight)) }
            }
        }

        // FAB rechts unten
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        ) { Icon(Icons.Filled.Add, contentDescription = "Schicht hinzufügen") }

        // Zurück links unten (wie zuvor)
        FilledTonalIconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(16.dp)
                .size(56.dp)
        ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück") }
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

/* Helper für Karten-Text */
private fun presentShift(s: Shift, hourlyWage: Double): List<String> {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(s.startEpochMillis).atZone(zone)
    val end = Instant.ofEpochMilli(s.endEpochMillis).atZone(zone)
    val dateStr = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(start)
    val timeRange = "%02d:%02d – %02d:%02d (Pause %d min)"
        .format(start.hour, start.minute, end.hour, end.minute, s.breakMinutes)
    val dMin = computeDurationMinutes(s.startEpochMillis, s.endEpochMillis, s.breakMinutes)
    val durStr = "Dauer: " + formatHours(dMin)
    val eurStr = "€: " + "%.2f".format((dMin / 60.0) * hourlyWage)
    return listOf(dateStr, timeRange, durStr, eurStr)
}
