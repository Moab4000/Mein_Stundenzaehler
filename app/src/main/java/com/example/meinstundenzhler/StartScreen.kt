@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.meinstundenzhler

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.launch
import java.time.LocalDate
import com.example.meinstundenzhler.data.MonthlyList
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.utils.MONTHS

@Composable
fun StartScreen(
    onBack: () -> Unit,
    repository: MonthlyListRepository,
    onSaved: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Defaults
    val now = LocalDate.now()
    var monthIndex by remember { mutableStateOf(now.monthValue - 1) } // 0..11
    var year by remember { mutableStateOf(now.year) }

    var monthMenuOpen by remember { mutableStateOf(false) }
    var yearMenuOpen by remember { mutableStateOf(false) }

    // Jahr-Liste (z. B. 10 Jahre zurück, 3 Jahre vor)
    val YEARS_BEFORE = 10
    val YEARS_AFTER = 3
    val years = remember(now.year) {
        ((now.year - YEARS_BEFORE)..(now.year + YEARS_AFTER)).toList().reversed()
    }

    var wageText by remember { mutableStateOf("") }
    var monthlyIncomeText by remember { mutableStateOf("") }
    var carryText by remember { mutableStateOf("") }

    fun parseDoubleOrNull(s: String): Double? =
        s.trim().replace(',', '.').toDoubleOrNull()

    val wage = parseDoubleOrNull(wageText)
    val income = monthlyIncomeText.trim().takeIf { it.isNotEmpty() }?.let(::parseDoubleOrNull)
    val carry = carryText.trim().takeIf { it.isNotEmpty() }?.let(::parseDoubleOrNull) ?: 0.0

    val canSave = wage != null && (monthlyIncomeText.isBlank() || income != null)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Liste erstellen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monat + Jahr nebeneinander
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {

                // Monat
                ExposedDropdownMenuBox(
                    expanded = monthMenuOpen,
                    onExpandedChange = { monthMenuOpen = it },
                    modifier = Modifier.weight(2f)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        value = MONTHS[monthIndex],
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Monat der Liste") }
                    )
                    ExposedDropdownMenu(
                        expanded = monthMenuOpen,
                        onDismissRequest = { monthMenuOpen = false }
                    ) {
                        MONTHS.forEachIndexed { idx, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = { monthIndex = idx; monthMenuOpen = false }
                            )
                        }
                    }
                }

                // Jahr
                ExposedDropdownMenuBox(
                    expanded = yearMenuOpen,
                    onExpandedChange = { yearMenuOpen = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        value = year.toString(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Jahr") }
                    )
                    ExposedDropdownMenu(
                        expanded = yearMenuOpen,
                        onDismissRequest = { yearMenuOpen = false }
                    ) {
                        years.forEach { y ->
                            DropdownMenuItem(
                                text = { Text(y.toString()) },
                                onClick = { year = y; yearMenuOpen = false }
                            )
                        }
                    }
                }
            }

            // Stundenlohn
            OutlinedTextField(
                value = wageText,
                onValueChange = { wageText = it },
                label = { Text("Stundenlohn") },
                prefix = { Text("€ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = {
                    if (wage == null && wageText.isNotBlank()) Text("Bitte Zahl eingeben (z. B. 14.50)")
                },
                isError = wage == null && wageText.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            // Monatlicher Verdienst (optional)
            OutlinedTextField(
                value = monthlyIncomeText,
                onValueChange = { monthlyIncomeText = it },
                label = { Text("Monatlicher Verdienst (optional)") },
                prefix = { Text("€ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = {
                    if (monthlyIncomeText.isNotBlank() && income == null)
                        Text("Bitte Zahl eingeben oder Feld leer lassen")
                },
                isError = monthlyIncomeText.isNotBlank() && income == null,
                modifier = Modifier.fillMaxWidth()
            )

            // Übertrag Vormonat (±)
            OutlinedTextField(
                value = carryText,
                onValueChange = { carryText = it },
                label = { Text("Übertrag Vormonat (±)") },
                prefix = { Text("€ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = { Text("Leer = 0,00 €") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // CTA
            Button(
                onClick = {
                    if (!canSave) return@Button
                    scope.launch {
                        val ml = MonthlyList(
                            monthIndex = monthIndex,
                            year = year,                  // << ausgewähltes Jahr verwenden
                            hourlyWage = wage!!,
                            previousDebt = carry,
                            monthlyIncome = income
                        )
                        repository.insert(ml)
                        onSaved()
                    }
                },
                enabled = canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) { Text("Liste erstellen") }
        }
    }
}
