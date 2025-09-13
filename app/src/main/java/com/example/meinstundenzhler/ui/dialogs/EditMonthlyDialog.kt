package com.example.meinstundenzhler.ui.detail.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.MonthlyList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMonthlyDialog(
    initial: MonthlyList,
    onDismiss: () -> Unit,
    onSave: (MonthlyList) -> Unit
) {
    val months = listOf(
        "Januar","Februar","März","April","Mai","Juni",
        "Juli","August","September","Oktober","November","Dezember"
    )

    var monthExpanded by remember { mutableStateOf(false) }
    var monthIndex by remember { mutableStateOf(initial.monthIndex) }
    var yearText by remember { mutableStateOf(initial.year.toString()) }
    var wageText by remember { mutableStateOf("%.2f".format(initial.hourlyWage)) }
    var debtText by remember { mutableStateOf("%.2f".format(initial.previousDebt)) } // ±
    var incomeText by remember { mutableStateOf(initial.monthlyIncome?.let { "%.2f".format(it) } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Liste bearbeiten") },
        text = {
            Column {
                // Monat
                ExposedDropdownMenuBox(
                    expanded = monthExpanded,
                    onExpandedChange = { monthExpanded = !monthExpanded },
                ) {
                    OutlinedTextField(
                        value = months[monthIndex],
                        onValueChange = { },
                        label = { Text("Monat") },
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor() // kompatibel (depr. Warnung ok)
                            .fillMaxWidth(),
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

                Spacer(Modifier.height(10.dp))

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

                // Übertrag (±)
                OutlinedTextField(
                    value = debtText,
                    onValueChange = { raw ->
                        val s = raw.replace(',', '.')
                        var minusSeen = false
                        var dotSeen = false
                        val out = StringBuilder()
                        s.forEachIndexed { i, ch ->
                            when {
                                ch.isDigit() -> out.append(ch)
                                ch == '-' && i == 0 && !minusSeen -> { out.append(ch); minusSeen = true }
                                ch == '.' && !dotSeen           -> { out.append(ch); dotSeen = true }
                            }
                        }
                        debtText = if (out.isEmpty() && raw.startsWith("-")) "-" else out.toString()
                    },
                    label = { Text("Übertrag Vormonat (±)") },
                    singleLine = true,
                    suffix = { Text("€") }
                )

                // Monatsverdienst (optional)
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
                        previousDebt = carry,
                        monthlyIncome = income
                    )
                )
            }) { Text("Speichern") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Abbrechen") } }
    )
}
