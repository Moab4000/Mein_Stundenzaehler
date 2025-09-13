package com.example.meinstundenzhler.ui.detail.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import com.example.meinstundenzhler.data.Shift
import com.example.meinstundenzhler.ui.detail.components.TimeField
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftEditorDialog(
    title: String,
    initial: Shift?, // null = neu, sonst bearbeiten
    onDismiss: () -> Unit,
    onSave: (LocalDate, Pair<Int, Int>, Pair<Int, Int>, Int, String) -> Unit
) {
    val zone = ZoneId.systemDefault()
    val initStart = initial?.let { Instant.ofEpochMilli(it.startEpochMillis).atZone(zone) }
    val initEnd = initial?.let { Instant.ofEpochMilli(it.endEpochMillis).atZone(zone) }

    // Defaults: neu => 00:00; bearbeiten => vorhandene Zeiten
    var date by remember { mutableStateOf(initStart?.toLocalDate() ?: LocalDate.now()) }
    var startH by remember { mutableStateOf(initStart?.hour ?: 0) }
    var startM by remember { mutableStateOf(initStart?.minute ?: 0) }
    var endH by remember { mutableStateOf(initEnd?.hour ?: 0) }
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

                // Zeitfelder (Numerische Eingabe, Auto-„:“, „Jetzt“-Taste)
                TimeField(
                    label = "Startzeit (HH:MM)",
                    value = startH to startM,
                    onValueChange = { h, m -> startH = h; startM = m }
                )
                TimeField(
                    label = "Endzeit (HH:MM)",
                    value = endH to endM,
                    onValueChange = { h, m -> endH = h; endM = m }
                )

                // Pause in Minuten + ±5
                Column {
                    OutlinedTextField(
                        value = breakMin.toString(),
                        onValueChange = { v -> breakMin = v.filter { it.isDigit() }.toIntOrNull() ?: 0 },
                        label = { Text("Pause (Minuten)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { breakMin = (breakMin - 5).coerceAtLeast(0) }) { Text("−5") }
                        FilledTonalButton(onClick = { breakMin += 5 }) { Text("+5") }
                    }
                }

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
