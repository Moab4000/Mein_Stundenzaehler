package com.example.meinstundenzhler.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.*

@Composable
fun MonthlyListCard(
    title: String,                  // z.B. "September 2025"
    workMinutes: Int,               // 833 -> 13:53 h
    earnedWithoutCarry: Double,     // 194.37
    monthlyIncome: Double?,         // 556.00 oder null
    carryThisMonth: Double?,        // -311.63 oder null
    onClick: () -> Unit,            // in Detail öffnen
    onDelete: () -> Unit,           // löschen
    modifier: Modifier = Modifier
) {
    val currency = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    val num = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
    val numStrong = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum")

    ElevatedCard(
        onClick = onClick,
        shape = MaterialTheme.shapes.extraLarge,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            // Kopfzeile
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                FilledTonalIconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Löschen")
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            // Werte (sauber, tabellarische Ziffern)
            KV("Arbeitszeit", formatHm(workMinutes), num, numStrong)
            KV("Verdient (ohne Übertrag)", currency.format(earnedWithoutCarry), num, numStrong)

            monthlyIncome?.let {
                KV("Monatlicher Verdienst", currency.format(it), num, numStrong)
            }

            carryThisMonth?.let { c ->
                val color =
                    if (c < 0) MaterialTheme.colorScheme.error
                    else Color(0xFF2E7D32) // ein dunkles Grün
                KV(
                    "Übertrag (dieser Monat)",
                    (if (c >= 0) "+" else "−") + currency.format(kotlin.math.abs(c)),
                    num,
                    numStrong,
                    valueColor = color
                )
            }
        }
    }
}

@Composable
private fun KV(
    label: String,
    value: String,
    baseStyle: androidx.compose.ui.text.TextStyle,
    strongStyle: androidx.compose.ui.text.TextStyle,
    valueColor: Color? = null
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

private fun formatHm(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "%d:%02d h".format(h, m)
}
