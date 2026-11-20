package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.AppSettings
import com.example.data.local.NoteCategory
import com.example.data.local.PersonalNote
import com.example.domain.LocalizationHelper
import com.example.ui.components.IconBadge3D
import com.example.ui.components.NotebookPageCard
import com.example.ui.viewmodel.LiveClockState
import java.util.Calendar
import java.util.GregorianCalendar
import java.util.TimeZone

@Composable
fun PersonalNotesScreen(
    notes: List<PersonalNote>,
    searchQuery: String,
    categoryFilter: NoteCategory?,
    settings: AppSettings,
    clock: LiveClockState,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (NoteCategory?) -> Unit,
    onSaveNote: (PersonalNote) -> Unit,
    onTogglePin: (PersonalNote) -> Unit,
    onDeleteNote: (PersonalNote) -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits

    var showNoteEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<PersonalNote?>(null) }
    var noteToDelete by remember { mutableStateOf<PersonalNote?>(null) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(if (isBn) "নোট, স্মরণিকা, পরিকল্পনা খুঁজুন..." else "Search personal notes...")
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            editingNote = null
                            showNoteEditor = true
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .testTag("add_personal_note_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (isBn) "নোট" else "New")
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = categoryFilter == null,
                        onClick = { onCategoryFilterChange(null) },
                        label = { Text(if (isBn) "সব নোট" else "All Notes") }
                    )
                    NoteCategory.entries.forEach { cat ->
                        val selected = categoryFilter == cat
                        FilterChip(
                            selected = selected,
                            onClick = { onCategoryFilterChange(if (selected) null else cat) },
                            label = { Text(cat.label(isBn)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = cat.accentColor.copy(alpha = 0.18f),
                                selectedLabelColor = cat.accentColor
                            )
                        )
                    }
                }
            }

            if (notes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconBadge3D(
                            icon = Icons.Filled.EditNote,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = null,
                            size = 64.dp
                        )
                        Text(
                            text = if (isBn) "আপনার ব্যক্তিগত নোটবুক খালি" else "Personal Notebook is Empty",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "গুরুত্বপূর্ণ নোট, স্মরণিকা, কাজের তালিকা বা ভবিষ্যৎ পরিকল্পনা লিখে রাখতে '+ নোট' বাটনে চাপ দিন।"
                            else "Tap '+ New' to write notes, memoirs, to-do lists, or future plans.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        val cat = note.categoryEnum
                        val dateText = LocalizationHelper.formatDate(
                            note.year, note.month, note.day, isBn, useBnDigits, settings.dateFormat
                        )
                        NotebookPageCard(accentColor = cat.accentColor) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = cat.accentColor.copy(alpha = 0.14f),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = cat.label(isBn),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = cat.accentColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Text(
                                        text = dateText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = note.content,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (note.extraComment.isNotBlank()) {
                                    Text(
                                        text = "${if (isBn) "মন্তব্য:" else "Note:"} ${note.extraComment}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    IconButton(onClick = { onTogglePin(note) }) {
                                        Icon(
                                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = if (isBn) "পিন" else "Pin",
                                            tint = if (note.isPinned) cat.accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(onClick = {
                                        editingNote = note
                                        showNoteEditor = true
                                    }) {
                                        Icon(Icons.Filled.Edit, contentDescription = if (isBn) "সম্পাদনা" else "Edit", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { noteToDelete = note }) {
                                        Icon(Icons.Filled.Delete, contentDescription = if (isBn) "মুছুন" else "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNoteEditor) {
        NoteEditorDialog(
            initial = editingNote,
            settings = settings,
            clock = clock,
            onDismiss = { showNoteEditor = false },
            onSave = { note ->
                onSaveNote(note)
                showNoteEditor = false
            }
        )
    }

    noteToDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text(if (isBn) "নোট মুছে ফেলবেন?" else "Delete Note?") },
            text = { Text(if (isBn) "'${note.title}' নোটটি স্থায়ীভাবে মুছে ফেলতে চান?" else "Permanently delete '${note.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteNote(note)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(if (isBn) "মুছুন" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text(if (isBn) "বাতিল" else "Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NoteEditorDialog(
    initial: PersonalNote?,
    settings: AppSettings,
    clock: LiveClockState,
    onDismiss: () -> Unit,
    onSave: (PersonalNote) -> Unit
) {
    val isBn = settings.isBangla
    val useBnDigits = settings.useBanglaDigits

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var content by remember { mutableStateOf(initial?.content ?: "") }
    var extraComment by remember { mutableStateOf(initial?.extraComment ?: "") }
    var category by remember { mutableStateOf(initial?.categoryEnum ?: NoteCategory.IMPORTANT_NOTE) }
    var isPinned by remember { mutableStateOf(initial?.isPinned ?: false) }
    var year by remember { mutableIntStateOf(initial?.year ?: clock.year) }
    var month by remember { mutableIntStateOf(initial?.month ?: clock.month) }
    var day by remember { mutableIntStateOf(initial?.day ?: clock.day) }
    var showDatePicker by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initial != null) (if (isBn) "নোট সম্পাদনা" else "Edit Note") else (if (isBn) "নতুন ব্যক্তিগত নোট" else "New Personal Note"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    NoteCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat.label(isBn)) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        titleError = false
                    },
                    label = { Text(if (isBn) "শিরোনাম *" else "Title *") },
                    isError = titleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(if (isBn) "বিস্তারিত লিখুন" else "Note Content") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(LocalizationHelper.formatDate(year, month, day, isBn, useBnDigits, settings.dateFormat))
                    }
                    FilterChip(
                        selected = isPinned,
                        onClick = { isPinned = !isPinned },
                        label = { Text(if (isBn) "পিন করুন" else "Pin") },
                        leadingIcon = {
                            Icon(Icons.Filled.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }

                OutlinedTextField(
                    value = extraComment,
                    onValueChange = { extraComment = it },
                    label = { Text(if (isBn) "অতিরিক্ত মন্তব্য (ঐচ্ছিক)" else "Extra Comment (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (isBn) "বাতিল" else "Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.trim().isEmpty()) {
                                titleError = true
                                return@Button
                            }
                            onSave(
                                PersonalNote(
                                    id = initial?.id ?: 0L,
                                    title = title.trim(),
                                    content = content.trim(),
                                    category = category.name,
                                    year = year,
                                    month = month,
                                    day = day,
                                    isPinned = isPinned,
                                    extraComment = extraComment.trim(),
                                    createdAtMillis = initial?.createdAtMillis ?: System.currentTimeMillis(),
                                    updatedAtMillis = System.currentTimeMillis()
                                )
                            )
                        }
                    ) {
                        Text(if (isBn) "সংরক্ষণ করুন" else "Save Note")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val utcInitMillis = remember(year, month, day) {
            GregorianCalendar(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(year, month - 1, day, 12, 0, 0)
            }.timeInMillis
        }
        val state = rememberDatePickerState(initialSelectedDateMillis = utcInitMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { ms ->
                            val c = GregorianCalendar(TimeZone.getTimeZone("UTC")).apply { timeInMillis = ms }
                            year = c.get(Calendar.YEAR)
                            month = c.get(Calendar.MONTH) + 1
                            day = c.get(Calendar.DAY_OF_MONTH)
                        }
                        showDatePicker = false
                    }
                ) { Text(if (isBn) "নির্বাচন" else "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(if (isBn) "বাতিল" else "Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
