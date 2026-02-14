package com.example.meinstundenzhler.ui.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.Shift
import com.example.meinstundenzhler.utils.computeDurationMinutes
import com.example.meinstundenzhler.utils.formatHours
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun ShiftItemCard(
    shift: Shift,
    hourlyWage: Double,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(shift.startEpochMillis).atZone(zone)
    val end   = Instant.ofEpochMilli(shift.endEpochMillis).atZone(zone)
    val df = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val dMin = computeDurationMinutes(shift.startEpochMillis, shift.endEpochMillis, shift.breakMinutes)
    val money = NumberFormat.getCurrencyInstance(Locale.GERMANY).format((dMin / 60.0) * hourlyWage)

    val num  = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
    val numB = MaterialTheme.typography.titleSmall.copy(fontFeatureSettings = "tnum")

    ElevatedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(df.format(start), style = MaterialTheme.typography.labelLarge)
                    val baseRange = "%02d:%02d – %02d:%02d"
                        .format(start.hour, start.minute, end.hour, end.minute)
                    val range = if (shift.breakMinutes > 0)
                        "$baseRange (Pause ${shift.breakMinutes} min)"
                    else baseRange
                    Text(range, style = MaterialTheme.typography.bodyMedium)
                    if (!shift.note.isNullOrBlank()) {
                        Text("Notiz: ${shift.note}", style = MaterialTheme.typography.bodySmall)
                    }
                }
                FilledTonalIconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dauer", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatHours(dMin), style = numB, textAlign = TextAlign.End)
                    Text(money, style = num, textAlign = TextAlign.End)
                }
            }
        }
    }
}
