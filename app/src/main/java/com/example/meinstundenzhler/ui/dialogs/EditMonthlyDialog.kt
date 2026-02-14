package com.example.meinstundenzhler.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.example.meinstundenzhler.data.MonthlyList

@Composable
fun EditMonthlyDialog(
    initial: MonthlyList,
    onDismiss: () -> Unit,
    onSave: (MonthlyList) -> Unit
) {
    var wageText by remember { mutableStateOf("%.2f".format(initial.hourlyWage)) }
    var prevDebtText by remember { mutableStateOf("%.2f".format(initial.previousDebt)) }
    var monthlyIncomeText by remember { mutableStateOf(initial.monthlyIncome?.let { "%.2f".format(it) } ?: "") }

    val wage = wageText.replace(',', '.').toDoubleOrNull()
    val prev = prevDebtText.replace(',', '.').toDoubleOrNull()
    val inc  = monthlyIncomeText.trim().takeIf { it.isNotEmpty() }?.replace(',', '.')?.toDoubleOrNull()

    val canSave = wage != null && prev != null && (monthlyIncomeText.isBlank() || inc != null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monatsdaten bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = wageText,
                    onValueChange = { wageText = it },
                    label = { Text("Stundenlohn (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prevDebtText,
                    onValueChange = { prevDebtText = it },
                    label = { Text("Übertrag Vormonat (€)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = monthlyIncomeText,
                    onValueChange = { monthlyIncomeText = it },
                    label = { Text("Monatlicher Verdienst (€) – optional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(enabled = canSave, onClick = {
                onSave(
                    initial.copy(
                        hourlyWage = wage ?: initial.hourlyWage,
                        previousDebt = prev ?: initial.previousDebt,
                        monthlyIncome = if (monthlyIncomeText.isBlank()) null else inc
                    )
                )
            }) { Text("Speichern") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        }
    )
}
