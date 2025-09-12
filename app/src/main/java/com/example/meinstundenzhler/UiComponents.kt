package com.example.meinstundenzhler

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.layout.PaddingValues


@Composable
fun AppTitle(text: String) {
    Text(text = text, fontSize = 24.sp)
}

@Composable
fun PrimaryActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPaddingH: Dp = 16.dp,
    contentPaddingV: Dp = 10.dp,
    textSizeSp: Int = 16
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = contentPaddingH,
            vertical = contentPaddingV
        )
    ) {
        Text(label, fontSize = textSizeSp.sp)
    }
}
