@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.meinstundenzhler.ui.detail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
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
fun MonthlySummaryCard(
    monthTitle: String,
    hourlyWage: Double,
    carryOver: Double,
    totalMinutes: Long,
    totalEarnings: Double,
    monthlyEarnings: Double?,
    monthCarryOver: Double?,
    onEditClick: () -> Unit,
    onPdfClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    showTitle: Boolean = true
) {
    val currency = NumberFormat.getCurrencyInstance(Locale.GERMANY)
    val num  = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum")
    val numB = MaterialTheme.typography.titleMedium.copy(fontFeatureSettings = "tnum")

    ElevatedCard(shape = MaterialTheme.shapes.extraLarge, modifier = modifier) {
        Column(Modifier.padding(if (compact) 12.dp else 16.dp)) {

            // Kopfzeile: links Titel ODER (wenn kein Titel) die Pills; rechts die Aktionen
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showTitle) {
                    Text(
                        text = monthTitle,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Pills in der Kopfzeile, damit links kein leerer Bereich bleibt
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InfoPill(
                            text = "Lohn: %.2f €/h".format(hourlyWage),
                            container = MaterialTheme.colorScheme.secondaryContainer,
                            content   = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        val negCarry = carryOver < 0
                        InfoPill(
                            text = "Übertrag: %+.2f €".format(carryOver),
                            container = if (negCarry) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.tertiaryContainer,
                            content   = if (negCarry) MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalIconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(if (compact) 36.dp else 40.dp)
                    ) { Icon(Icons.Filled.Edit, contentDescription = "Bearbeiten") }

                    FilledTonalIconButton(
                        onClick = onPdfClick,
                        modifier = Modifier.size(if (compact) 36.dp else 40.dp)
                    ) { Icon(Icons.Filled.PictureAsPdf, contentDescription = "PDF") }
                }
            }

            // Falls wir den Titel zeigen, kommen die Pills eine Zeile darunter
            if (showTitle) {
                Spacer(Modifier.height(if (compact) 6.dp else 10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(
                        text = "Lohn: %.2f €/h".format(hourlyWage),
                        container = MaterialTheme.colorScheme.secondaryContainer,
                        content   = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    val negCarry = carryOver < 0
                    InfoPill(
                        text = "Übertrag: %+.2f €".format(carryOver),
                        container = if (negCarry) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.tertiaryContainer,
                        content   = if (negCarry) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            Spacer(Modifier.height(if (compact) 8.dp else 12.dp))
            Divider()
            Spacer(Modifier.height(if (compact) 8.dp else 12.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Summe", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatHours(totalMinutes.toInt()), style = numB, textAlign = TextAlign.End)
                    Text(currency.format(totalEarnings), style = num, textAlign = TextAlign.End)
                }
            }

            monthlyEarnings?.let {
                Spacer(Modifier.height(if (compact) 6.dp else 10.dp))
                Divider()
                Spacer(Modifier.height(if (compact) 6.dp else 10.dp))

                KV("Monatlicher Verdienst", currency.format(it), num)

                val carry = monthCarryOver ?: 0.0
                val color = if (carry < 0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
                KV(
                    "Übertrag (dieser Monat)",
                    (if (carry >= 0) "+" else "−") + currency.format(kotlin.math.abs(carry)),
                    num,
                    valueColor = color
                )
            }
        }
    }
}

/* ---- kleine Hilfen ---- */

@Composable
private fun InfoPill(
    text: String,
    container: Color,
    content: Color
) {
    Surface(
        color = container,
        contentColor = content,
        shape = CircleShape,
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun KV(
    label: String,
    value: String,
    style: androidx.compose.ui.text.TextStyle,
    valueColor: Color? = null
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = style, modifier = Modifier.weight(1f))
        Text(value, style = style, color = valueColor ?: LocalContentColor.current, textAlign = TextAlign.End)
    }
}

private fun formatHours(min: Int): String {
    val h = min / 60
    val m = min % 60
    return "%d:%02d h".format(h, m)
}
