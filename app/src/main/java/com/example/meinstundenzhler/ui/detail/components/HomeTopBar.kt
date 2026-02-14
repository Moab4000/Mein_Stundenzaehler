@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.meinstundenzhler.ui.detail.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeTopBar(
    onTipsClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = { Text("Mein Stundenzähler") },
        navigationIcon = {
            IconButton(onClick = onTipsClick) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = "Tipps")
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Outlined.Settings, contentDescription = "Einstellungen")
            }
        }
    )
}
