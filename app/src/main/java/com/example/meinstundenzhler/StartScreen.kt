package com.example.meinstundenzhler

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.MonthlyList
import com.example.meinstundenzhler.data.MonthlyListRepository
import com.example.meinstundenzhler.ui.theme.MeinStundenzählerTheme
import kotlinx.coroutines.launch
import java.time.LocalDate



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartScreen(
    onBack: () -> Unit,
    repository: MonthlyListRepository,
    onSaved: (Long) -> Unit   // nach dem Speichern zur Detail-Seite mit neuer ID
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val months = listOf(
        "Januar","Februar","März","April","Mai","Juni",
        "Juli","August","September","Oktober","November","Dezember"
    )
    val currentMonthIndex = remember { LocalDate.now().monthValue - 1 }
    val currentYear = remember { LocalDate.now().year }

    var monthExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedMonthIndex by rememberSaveable { mutableStateOf(currentMonthIndex) }

    var wageText by rememberSaveable { mutableStateOf("") }           // €/h
    var monthlyIncomeText by rememberSaveable { mutableStateOf("") }  // € (optional)
    var carryText by rememberSaveable { mutableStateOf("") }          // Übertrag (±), optional

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Inhalt
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.9f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Monat wählen
            ExposedDropdownMenuBox(
                expanded = monthExpanded,
                onExpandedChange = { monthExpanded = !monthExpanded },
            ) {
                OutlinedTextField(
                    value = months[selectedMonthIndex],
                    onValueChange = { },
                    label = { Text("Monat der Liste") },
                    readOnly = true,
                    modifier = Modifier
                        .menuAnchor(
                        type = MenuAnchorType.PrimaryNotEditable, // Feld ist readOnly
                        enabled = true
                    )
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
                            onClick = { selectedMonthIndex = idx; monthExpanded = false }
                        )
                    }
                }
            }

            // Stundenlohn
            OutlinedTextField(
                value = wageText,
                onValueChange = { wageText = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' } },
                label = { Text("Stundenlohn") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("€/h") },
                modifier = Modifier.fillMaxWidth()
            )

            // Monatlicher Verdienst (optional)
            OutlinedTextField(
                value = monthlyIncomeText,
                onValueChange = { monthlyIncomeText = it.filter { ch -> ch.isDigit() || ch == ',' || ch == '.' } },
                label = { Text("Monatlicher Verdienst (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("€") },
                modifier = Modifier.fillMaxWidth()
            )

            // Übertrag (±) vom Vormonat
            OutlinedTextField(
                value = carryText,
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
                    carryText = if (out.isEmpty() && raw.startsWith("-")) "-" else out.toString()
                },
                label = { Text("Übertrag Vormonat (±)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                suffix = { Text("€") },
                modifier = Modifier.fillMaxWidth()
            )


            // Speichern
            Button(
                onClick = {
                    val wage = wageText.replace(',', '.').toDoubleOrNull()
                    val income = monthlyIncomeText.replace(',', '.').toDoubleOrNull()
                    val carry = carryText
                        .takeIf { it.isNotBlank() && it != "-" }
                        ?.replace(',', '.')
                        ?.toDoubleOrNull() ?: 0.0

                    if (wage == null || wage <= 0.0) {
                        Toast.makeText(ctx, "Bitte gültigen Stundenlohn eingeben.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val entity = MonthlyList(
                        year = currentYear,
                        monthIndex = selectedMonthIndex,
                        hourlyWage = wage,
                        monthlyIncome = income,
                        previousDebt = carry     // <- Übertrag speichern (kann + oder − sein)
                    )

                    scope.launch {
                        val newId = repository.insert(entity)
                        Toast.makeText(ctx, "Liste gespeichert.", Toast.LENGTH_SHORT).show()
                        onSaved(newId) // direkt zur Detail-Seite
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Liste erstellen")
            }
        }

        // Zurück unten links
        FilledTonalIconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 16.dp, bottom = 16.dp)
                .size(56.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StartScreenPreview() {
    MeinStundenzählerTheme {
        // Preview: keine echten Repos
    }
}
