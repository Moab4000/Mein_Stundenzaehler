package com.example.meinstundenzhler

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.MonthlyList
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.data.Shift
import com.example.meinstundenzhler.data.ShiftRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType


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

    var showAdd by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Shift?>(null) }
    var editing by remember { mutableStateOf<Shift?>(null) }
    var showEditList by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))
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
        }

        // Liste bearbeiten (Stift oben rechts)
        IconButton(
            onClick = { if (monthly != null) showEditList = true else Toast.makeText(ctx, "Liste lädt…", Toast.LENGTH_SHORT).show() },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(Icons.Filled.Edit, contentDescription = "Liste bearbeiten")
        }

        // Schichtenliste
        LazyColumn(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
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
        }

        // + Schicht
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        ) { Icon(Icons.Filled.Add, contentDescription = "Schicht hinzufügen") }

        // Zurück
        FilledTonalIconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size(56.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
        }
    }

    // --- Dialoge ---

    if (showEditList && monthly != null) {
        EditMonthlyDialog(
            initial = monthly!!,
            onDismiss = { showEditList = false },
            onSave = { updated ->
                scope.launch {
                    monthlyRepo.update(updated)
                    showEditList = false
                }
            }
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
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Abbrechen") }
            }
        )
    }
}

/* ------------------ Hilfsfunktionen ------------------ */

private fun computeDurationMinutes(startMs: Long, endMs: Long, breakMin: Int): Int {
    val raw = ((endMs - startMs) / 60_000L).toInt()
    return (raw - breakMin).coerceAtLeast(0)
}

private fun formatHours(totalMin: Int): String {
    val h = totalMin / 60
    val m = totalMin % 60
    return "%d:%02d".format(h, m)
}

private fun monthName(index: Int): String = listOf(
    "Januar","Februar","März","April","Mai","Juni","Juli","August","September","Oktober","November","Dezember"
)[index.coerceIn(0, 11)]

private fun presentShift(s: Shift, hourlyWage: Double): List<String> {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(s.startEpochMillis).atZone(zone)
    val end = Instant.ofEpochMilli(s.endEpochMillis).atZone(zone)

    val dateStr = DateTimeFormatter.ofPattern("dd.MM.yyyy").format(start)
    val timeRange = "%02d:%02d – %02d:%02d (Pause %d min)"
        .format(start.hour, start.minute, end.hour, end.minute, s.breakMinutes)

    val durMin = computeDurationMinutes(s.startEpochMillis, s.endEpochMillis, s.breakMinutes)
    val durStr = "Dauer: " + formatHours(durMin)
    val eurStr = "€: " + "%.2f".format((durMin / 60.0) * hourlyWage)

    return listOf(dateStr, timeRange, durStr, eurStr)
}

/* ------------------ Dialoge ------------------ */

// Liste (Monat/Jahr/Lohn/Schulden/optional Einkommen) bearbeiten
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditMonthlyDialog(
    initial: com.example.meinstundenzhler.data.MonthlyList,
    onDismiss: () -> Unit,
    onSave: (com.example.meinstundenzhler.data.MonthlyList) -> Unit
) {
    val months = listOf(
        "Januar","Februar","März","April","Mai","Juni",
        "Juli","August","September","Oktober","November","Dezember"
    )

    var monthExpanded by remember { mutableStateOf(false) }
    var monthIndex by remember { mutableStateOf(initial.monthIndex) }
    var yearText by remember { mutableStateOf(initial.year.toString()) }
    var wageText by remember { mutableStateOf("%.2f".format(initial.hourlyWage)) }
    var debtText by remember { mutableStateOf("%.2f".format(initial.previousDebt)) } // Übertrag (±)
    var incomeText by remember { mutableStateOf(initial.monthlyIncome?.let { "%.2f".format(it) } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Liste bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Monat (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = !monthExpanded },
                ) {
                    OutlinedTextField(
                        value = months[monthIndex],
                        onValueChange = { },
                        label = { Text("Monat") },
                        readOnly = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthExpanded) }
                    )
                    ExposedDropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        months.forEachIndexed { idx, m ->
                            DropdownMenuItem(
                                text = { Text(m) },
                                onClick = { monthIndex = idx; monthExpanded = false }
                            )
                        }
                    }
                }

                // Jahr
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { yearText = it.filter { ch -> ch.isDigit() }.take(4) },
                    label = { Text("Jahr") },
                    singleLine = true
                )

                // Stundenlohn
                OutlinedTextField(
                    value = wageText,
                    onValueChange = { wageText = it.filter { ch -> ch.isDigit() || ch==',' || ch=='.' } },
                    label = { Text("Stundenlohn") },
                    singleLine = true,
                    suffix = { Text("€/h") }
                )

                // ÜBERTRAG (±) – Minus erlauben
                OutlinedTextField(
                    value = debtText,
                    onValueChange = { raw ->
                        // Erlaube: Ziffern, EIN führendes '-', EIN Dezimalpunkt/Komma
                        val s = raw.replace(',', '.')
                        var minusSeen = false
                        var dotSeen = false
                        val out = StringBuilder()
                        s.forEachIndexed { i, ch ->
                            when {
                                ch.isDigit() -> out.append(ch)
                                ch == '-' && i == 0 && !minusSeen -> { out.append(ch); minusSeen = true }
                                ch == '.' && !dotSeen           -> { out.append(ch); dotSeen = true }
                                // sonst ignorieren
                            }
                        }
                        // Zwischenzustand "-" erlauben, damit man weitertippen kann
                        debtText = if (out.isEmpty() && raw.startsWith("-")) "-" else out.toString()
                    },
                    label = { Text("Übertrag Vormonat (±)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text), // Text → zeigt Minus auf Tastatur
                    singleLine = true,
                    suffix = { Text("€") }
                )

                // Monatlicher Verdienst (optional)
                OutlinedTextField(
                    value = incomeText,
                    onValueChange = { incomeText = it.filter { ch -> ch.isDigit() || ch==',' || ch=='.' } },
                    label = { Text("Monatlicher Verdienst (optional)") },
                    singleLine = true,
                    suffix = { Text("€") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val year = yearText.toIntOrNull() ?: return@TextButton
                val wage = wageText.replace(',', '.').toDoubleOrNull() ?: return@TextButton
                // „-” als Zwischenzustand nicht als 0.0 interpretieren
                val carry = debtText
                    .takeIf { it.isNotBlank() && it != "-" }
                    ?.replace(',', '.')
                    ?.toDoubleOrNull() ?: 0.0
                val income = incomeText.takeIf { it.isNotBlank() }?.replace(',', '.')?.toDoubleOrNull()

                if (wage <= 0.0 || year !in 2000..2100) return@TextButton

                onSave(
                    initial.copy(
                        year = year,
                        monthIndex = monthIndex,
                        hourlyWage = wage,
                        previousDebt = carry,     // ±-Wert speichern
                        monthlyIncome = income
                    )
                )
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShiftEditorDialog(
    title: String,
    initial: Shift?, // null = neu, sonst bearbeiten
    onDismiss: () -> Unit,
    onSave: (LocalDate, Pair<Int, Int>, Pair<Int, Int>, Int, String) -> Unit
) {
    val zone = ZoneId.systemDefault()
    val initStart = initial?.let { Instant.ofEpochMilli(it.startEpochMillis).atZone(zone) }
    val initEnd = initial?.let { Instant.ofEpochMilli(it.endEpochMillis).atZone(zone) }

    var date by remember { mutableStateOf(initStart?.toLocalDate() ?: LocalDate.now()) }
    var startH by remember { mutableStateOf(initStart?.hour ?: 14) }
    var startM by remember { mutableStateOf(initStart?.minute ?: 0) }
    var endH by remember { mutableStateOf(initEnd?.hour ?: 20) }
    var endM by remember { mutableStateOf(initEnd?.minute ?: 0) }
    var breakMin by remember { mutableStateOf(initial?.breakMinutes ?: 0) }
    var note by remember { mutableStateOf(initial?.note ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Datum: ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { date = date.minusDays(1) }) { Text("− Tag") }
                    OutlinedButton(onClick = { date = date.plusDays(1) }) { Text("+ Tag") }
                }

                Text("Startzeit (HH:MM)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeNumberField(value = startH, onChange = { startH = it.coerceIn(0, 23) }, label = "HH")
                    TimeNumberField(value = startM, onChange = { startM = it.coerceIn(0, 59) }, label = "MM")
                }

                Text("Endzeit (HH:MM)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeNumberField(value = endH, onChange = { endH = it.coerceIn(0, 23) }, label = "HH")
                    TimeNumberField(value = endM, onChange = { endM = it.coerceIn(0, 59) }, label = "MM")
                }

                OutlinedTextField(
                    value = breakMin.toString(),
                    onValueChange = { v -> breakMin = v.filter { it.isDigit() }.toIntOrNull() ?: 0 },
                    label = { Text("Pause (Minuten)") },
                    singleLine = true
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notiz (optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    date,
                    startH to startM,
                    endH to endM,
                    breakMin,
                    note
                )
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}

@Composable
private fun TimeNumberField(
    value: Int,
    onChange: (Int) -> Unit,
    label: String
) {
    OutlinedTextField(
        value = "%02d".format(value),
        onValueChange = { s ->
            s.filter { it.isDigit() }
                .take(2)
                .toIntOrNull()
                ?.let(onChange)
        },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.width(90.dp)
    )
}
