package com.example.meinstundenzhler.ui.detail.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalTime

@Composable
fun TimeField(
    label: String,
    value: Pair<Int, Int>,
    onValueChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // interner State: Ziffern „HHMM“
    var digits by remember(value) { mutableStateOf("%02d%02d".format(value.first, value.second)) }

    OutlinedTextField(
        value = digits,
        onValueChange = { new ->
            val clean = new.filter(Char::isDigit).take(4)
            digits = clean
            if (clean.length == 4) {
                val h = clean.take(2).toInt()
                val m = clean.drop(2).toInt()
                if (h in 0..23 && m in 0..59) onValueChange(h, m)
            }
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        visualTransformation = TimeVisualTransformation(), // stellt „HH:MM“ dar
        suffix = {
            TextButton(
                onClick = {
                    val now = LocalTime.now()
                    digits = "%02d%02d".format(now.hour, now.minute)
                    onValueChange(now.hour, now.minute)
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.padding(end = 4.dp)
            ) { Text("Jetzt") }
        },
        modifier = modifier.fillMaxWidth()
    )
}

private class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.take(4) // HHMM
        val out = when {
            raw.length <= 2 -> raw + if (raw.length == 2) ":" else ""
            else -> raw.substring(0, 2) + ":" + raw.substring(2)
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = if (offset <= 2) offset else offset + 1
            override fun transformedToOriginal(offset: Int) = if (offset <= 2) offset else (offset - 1).coerceAtMost(4)
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}
