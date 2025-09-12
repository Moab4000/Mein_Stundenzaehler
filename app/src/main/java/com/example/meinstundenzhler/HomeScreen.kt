package com.example.meinstundenzhler

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onStartClick: () -> Unit,
    onListsClick: () -> Unit
) {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titel nicht ganz oben
        Spacer(modifier = Modifier.weight(0.10f))
        AppTitle(text = "Mein Stundenzähler")

        // Buttons ungefähr in der Mitte
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.80f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryActionButton(
                    label = "Start",
                    onClick = onStartClick,
                    modifier = Modifier
                        .width(240.dp)
                        .height(56.dp),
                    contentPaddingH = 24.dp,
                    contentPaddingV = 16.dp,
                    textSizeSp = 22
                )
                PrimaryActionButton(
                    label = "Meine Listen",
                    onClick = {
                        Toast.makeText(ctx, "Meine Listen gedrückt", Toast.LENGTH_SHORT).show()
                        onListsClick()
                    },
                    modifier = Modifier
                        .width(240.dp)
                        .height(56.dp),
                    contentPaddingH = 24.dp,
                    contentPaddingV = 16.dp,
                    textSizeSp = 20
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.10f))
    }
}
