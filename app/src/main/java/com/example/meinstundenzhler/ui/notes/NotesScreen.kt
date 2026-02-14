@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.meinstundenzhler.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.meinstundenzhler.data.Note
import com.example.meinstundenzhler.data.NoteRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun NotesScreen(
    noteRepository: NoteRepository,
    onBack: () -> Unit,
    onNoteClick: (Note) -> Unit
) {
    val notes by noteRepository.getAllNotes().collectAsState(initial = emptyList())
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    val scope = rememberCoroutineScope()

    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Notiz löschen") },
            text = { Text("Möchten Sie diese Notiz wirklich löschen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            noteToDelete?.let { noteRepository.deleteNote(it) }
                            noteToDelete = null
                        }
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Meine Notizen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notes) { note ->
                NoteItem(
                    note = note,
                    onDeleteClick = { noteToDelete = note },
                    onNoteClick = { onNoteClick(note) }
                )
            }
        }
    }
}

@Composable
private fun NoteItem(note: Note, onDeleteClick: () -> Unit, onNoteClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd. MMMM yyyy").withZone(ZoneId.systemDefault())

    ElevatedCard(
        onClick = { onNoteClick() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = note.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = formatter.format(Instant.ofEpochMilli(note.dateMillis)),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            FilledTonalIconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = "Löschen")
            }
        }
    }
}
