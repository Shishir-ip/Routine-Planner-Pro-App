package com.shishir.routineplannerpro

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.shishir.routineplannerpro.model.RoutineCategoryEntity
import com.shishir.routineplannerpro.model.RoutineItemEntity
import com.shishir.routineplannerpro.model.RoutineType
import com.shishir.routineplannerpro.ui.AppUiState
import com.shishir.routineplannerpro.ui.AppViewModel
import com.shishir.routineplannerpro.util.DateTimeUtils
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels {
        AppViewModel.factory(AppContainer(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val dark by viewModel.darkTheme.collectAsStateWithLifecycle()
            MaterialTheme(colorScheme = if (dark) androidx.compose.material3.darkColorScheme() else androidx.compose.material3.lightColorScheme()) {
                AppScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScreen(viewModel: AppViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showAddRoutine by rememberSaveable { mutableStateOf(false) }
    var showAddItem by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    state.toastMessage?.let {
        LaunchedEffect(it) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.setMessage(null)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Routine Planner Pro", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = { showAddRoutine = true }) { Text("Add routine +") }
                    TextButton(onClick = { showSettings = true }) { Text("Settings") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddItem = true }) {
                Text("+")
            }
        }
    ) { padding ->
        RoutineContent(
            modifier = Modifier.padding(padding),
            state = state,
            onSelectCategory = viewModel::selectCategory,
            onDeleteCategory = viewModel::deleteCategory,
            onDeleteItem = viewModel::deleteItem,
            onChangeDate = viewModel::setSelectedDate
        )
    }

    if (showAddRoutine) {
        AddRoutineDialog(
            onDismiss = { showAddRoutine = false },
            onAdd = {
                viewModel.addCategory(it)
                showAddRoutine = false
            }
        )
    }

    if (showAddItem) {
        AddItemDialog(
            state = state,
            onDismiss = { showAddItem = false },
            onSave = {
                viewModel.addItem(it)
                showAddItem = false
            }
        )
    }

    if (showSettings) {
        SettingsDialog(viewModel = viewModel, state = state, onDismiss = { showSettings = false })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RoutineContent(
    modifier: Modifier,
    state: AppUiState,
    onSelectCategory: (Long) -> Unit,
    onDeleteCategory: (RoutineCategoryEntity) -> Unit,
    onDeleteItem: (RoutineItemEntity) -> Unit,
    onChangeDate: (LocalDate) -> Unit
) {
    val selected = state.categories.firstOrNull { it.id == state.selectedCategoryId }
    val scroll = rememberScrollState()

    Column(modifier = modifier.fillMaxSize().padding(12.dp)) {
        if (state.categories.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(scroll),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.categories.forEach { category ->
                    AssistChip(
                        onClick = { onSelectCategory(category.id) },
                        label = { Text(category.name) }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        DateSelector(state.selectedDate, onChangeDate)
        Spacer(Modifier.height(12.dp))

        val itemsForCategory = filteredItemsForCategory(state, selected)
        val categoryMap = state.categories.associateBy { it.id }

        if (itemsForCategory.isEmpty()) {
            Text("No activities for this date. Tap + to add.")
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsForCategory.forEach { item ->
                var expanded by remember(item.id) { mutableStateOf(false) }
                val itemCategory = categoryMap[item.categoryId]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { expanded = !expanded },
                            onLongClick = { onDeleteItem(item) }
                        )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text("${item.startTime} - ${item.endTime}")
                                if (item.roomNumber.isNotBlank()) Text("Room: ${item.roomNumber}")
                                if (itemCategory != null && itemCategory.type != RoutineType.DAILY && selected?.type == RoutineType.DAILY) {
                                    Text("from ${itemCategory.name}", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            TextButton(onClick = { expanded = !expanded }) { Text(if (expanded) "Hide" else "Details") }
                        }
                        AnimatedVisibility(
                            visible = expanded,
                            enter = fadeIn() + slideInVertically(),
                            exit = fadeOut() + slideOutVertically()
                        ) {
                            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (item.classType.isNotBlank()) Text("Type: ${item.classType}")
                                if (item.teacherName.isNotBlank()) Text("Teacher: ${item.teacherName}")
                                if (item.section.isNotBlank()) Text("Section: ${item.section}")
                                Text("Days: ${item.daysCsv}")
                                Text("Date Range: ${item.startDate ?: "Always"} to ${item.endDate ?: "Always"}")
                                if (item.reminderEnabled) Text("Reminder: ${item.reminderMinutesBefore} min before")
                                if (item.alarmEnabled) Text("Alarm: ${item.alarmMinutesBefore} min before")
                                if (item.additionalInfo.isNotBlank()) Text("More: ${item.additionalInfo}")
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        selected?.takeIf { !it.isSystem }?.let {
            OutlinedButton(onClick = { onDeleteCategory(it) }) { Text("Delete this routine") }
        }
    }
}

@Composable
private fun DateSelector(date: LocalDate, onChangeDate: (LocalDate) -> Unit) {
    var text by remember(date) { mutableStateOf(DateTimeUtils.formatDate(date)) }
    OutlinedTextField(
        value = text,
        onValueChange = {
            text = it
            DateTimeUtils.parseDate(it)?.let(onChangeDate)
        },
        label = { Text("View date (yyyy-MM-dd)") },
        modifier = Modifier.fillMaxWidth()
    )
}

private fun filteredItemsForCategory(state: AppUiState, selected: RoutineCategoryEntity?): List<RoutineItemEntity> {
    if (selected == null) return emptyList()
    val byDate = state.allItems.filter { DateTimeUtils.isActiveOnDate(it, state.selectedDate) }

    val base = byDate.filter { it.categoryId == selected.id }
    val extra = if (selected.type == RoutineType.DAILY) byDate.filter { it.categoryId != selected.id } else emptyList()

    return (base + extra).sortedBy { DateTimeUtils.parseTime(it.startTime)?.toSecondOfDay() ?: Int.MAX_VALUE }
}

@Composable
private fun AddRoutineDialog(onDismiss: () -> Unit, onAdd: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Routine +") },
        text = {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Routine name") })
        },
        confirmButton = { Button(onClick = { onAdd(name) }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun AddItemDialog(
    state: AppUiState,
    onDismiss: () -> Unit,
    onSave: (RoutineItemEntity) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var startTime by rememberSaveable { mutableStateOf("10:00 AM") }
    var endTime by rememberSaveable { mutableStateOf("11:00 AM") }
    var room by rememberSaveable { mutableStateOf("") }
    var classType by rememberSaveable { mutableStateOf("Theory") }
    var teacher by rememberSaveable { mutableStateOf("") }
    var section by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf("") }
    var endDate by rememberSaveable { mutableStateOf("") }
    var reminderEnabled by rememberSaveable { mutableStateOf(false) }
    var reminderMinutes by rememberSaveable { mutableStateOf("5") }
    var alarmEnabled by rememberSaveable { mutableStateOf(false) }
    var alarmMinutes by rememberSaveable { mutableStateOf("5") }
    var additionalInfo by rememberSaveable { mutableStateOf("") }
    var showMore by rememberSaveable { mutableStateOf(false) }
    val selectedDays = rememberSaveable { mutableStateOf(setOf("EVERYDAY")) }

    val categories = state.categories
    var selectedCategoryId by rememberSaveable { mutableStateOf(state.selectedCategoryId ?: categories.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Activity") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Activity/Course name") })

                var categoryExpanded by remember { mutableStateOf(false) }
                Box {
                    OutlinedButton(onClick = { categoryExpanded = true }) {
                        Text(categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "Select routine")
                    }
                    DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = startTime, onValueChange = { startTime = it }, modifier = Modifier.weight(1f), label = { Text("Start") })
                    OutlinedTextField(value = endTime, onValueChange = { endTime = it }, modifier = Modifier.weight(1f), label = { Text("End") })
                }

                OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room No. (optional)") })
                OutlinedTextField(value = classType, onValueChange = { classType = it }, label = { Text("Class Type / activity kind") })

                DaysPicker(selectedDays.value) { selectedDays.value = it }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = startDate, onValueChange = { startDate = it }, modifier = Modifier.weight(1f), label = { Text("Start date") })
                    OutlinedTextField(value = endDate, onValueChange = { endDate = it }, modifier = Modifier.weight(1f), label = { Text("End date") })
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Reminder")
                    Spacer(Modifier.width(6.dp))
                    Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = reminderMinutes,
                        onValueChange = { reminderMinutes = it },
                        modifier = Modifier.width(90.dp),
                        label = { Text("Min") }
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Alarm")
                    Spacer(Modifier.width(6.dp))
                    Switch(checked = alarmEnabled, onCheckedChange = { alarmEnabled = it })
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = alarmMinutes,
                        onValueChange = { alarmMinutes = it },
                        modifier = Modifier.width(90.dp),
                        label = { Text("Min") }
                    )
                }

                TextButton(onClick = { showMore = !showMore }) { Text(if (showMore) "Hide + options" else "+ More options") }
                AnimatedVisibility(visible = showMore) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = teacher, onValueChange = { teacher = it }, label = { Text("Teacher (optional)") })
                        OutlinedTextField(value = section, onValueChange = { section = it }, label = { Text("Section (optional)") })
                        OutlinedTextField(value = additionalInfo, onValueChange = { additionalInfo = it }, label = { Text("Additional info") })
                    }
                }
                Text("Tip: Long press activity card to delete.")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val categoryId = selectedCategoryId ?: return@Button
                    onSave(
                        RoutineItemEntity(
                            categoryId = categoryId,
                            title = title,
                            startTime = startTime,
                            endTime = endTime,
                            roomNumber = room,
                            classType = classType,
                            teacherName = teacher,
                            section = section,
                            additionalInfo = additionalInfo,
                            daysCsv = selectedDays.value.joinToString(","),
                            startDate = startDate.ifBlank { null },
                            endDate = endDate.ifBlank { null },
                            reminderEnabled = reminderEnabled,
                            reminderMinutesBefore = reminderMinutes.toIntOrNull() ?: 5,
                            alarmEnabled = alarmEnabled,
                            alarmMinutesBefore = alarmMinutes.toIntOrNull() ?: 5
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DaysPicker(current: Set<String>, onChange: (Set<String>) -> Unit) {
    val days = listOf(
        "EVERYDAY",
        DayOfWeek.SATURDAY.name,
        DayOfWeek.SUNDAY.name,
        DayOfWeek.MONDAY.name,
        DayOfWeek.TUESDAY.name,
        DayOfWeek.WEDNESDAY.name,
        DayOfWeek.THURSDAY.name,
        DayOfWeek.FRIDAY.name
    )

    Column {
        Text("Days")
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            days.forEach { day ->
                AssistChip(
                    onClick = {
                        val set = current.toMutableSet()
                        if (day == "EVERYDAY") {
                            onChange(setOf("EVERYDAY"))
                        } else {
                            set.remove("EVERYDAY")
                            if (!set.add(day)) set.remove(day)
                            onChange(if (set.isEmpty()) setOf("EVERYDAY") else set)
                        }
                    },
                    label = { Text(day.take(3)) }
                )
            }
        }
    }
}

@Composable
private fun SettingsDialog(viewModel: AppViewModel, state: AppUiState, onDismiss: () -> Unit) {
    val dark by viewModel.darkTheme.collectAsStateWithLifecycle()
    val apiKey by viewModel.apiKey.collectAsStateWithLifecycle()
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var apiInput by rememberSaveable { mutableStateOf(apiKey) }
    var exportJson by rememberSaveable { mutableStateOf("") }
    var importJson by rememberSaveable { mutableStateOf("") }
    var aiPrompt by rememberSaveable { mutableStateOf("") }
    var aiOutput by rememberSaveable { mutableStateOf("") }
    var exportRoutineExpanded by remember { mutableStateOf(false) }
    var selectedExport by remember { mutableStateOf(state.categories.firstOrNull()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Theme: Night mode")
                    Spacer(Modifier.weight(1f))
                    Switch(checked = dark, onCheckedChange = viewModel::toggleTheme)
                }

                Divider()
                Text("OpenRouter API key")
                OutlinedTextField(
                    value = apiInput,
                    onValueChange = { apiInput = it },
                    label = { Text("API key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Saved key: ${viewModel.maskedApiKey()}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.saveApiKey(apiInput) }) { Text("Save key") }
                    OutlinedButton(onClick = { viewModel.deleteApiKey(); apiInput = "" }) { Text("Delete key") }
                }

                Divider()
                Text("Routine export/import (JSON)")
                OutlinedButton(onClick = { exportRoutineExpanded = true }) {
                    Text(selectedExport?.name ?: "Select routine")
                }
                DropdownMenu(expanded = exportRoutineExpanded, onDismissRequest = { exportRoutineExpanded = false }) {
                    state.categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedExport = category
                                exportRoutineExpanded = false
                            }
                        )
                    }
                }
                Button(onClick = {
                    val selected = selectedExport ?: return@Button
                    scope.launch {
                        exportJson = viewModel.exportRoutine(selected)
                    }
                }) { Text("Generate export JSON") }
                OutlinedTextField(
                    value = exportJson,
                    onValueChange = { exportJson = it },
                    label = { Text("Export JSON") },
                    modifier = Modifier.fillMaxWidth().height(140.dp)
                )
                OutlinedTextField(
                    value = importJson,
                    onValueChange = { importJson = it },
                    label = { Text("Paste routine JSON to import") },
                    modifier = Modifier.fillMaxWidth().height(120.dp)
                )
                Button(onClick = { viewModel.importRoutineJson(importJson) }) { Text("Import JSON") }

                Divider()
                Text("AI Generator (OpenRouter free model)")
                OutlinedTextField(value = aiPrompt, onValueChange = { aiPrompt = it }, label = { Text("Describe routines in natural language") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    viewModel.generateFromAi(aiPrompt) { result, error ->
                        aiOutput = result ?: "Error: $error"
                    }
                }) { Text("Generate JSON with AI") }
                OutlinedTextField(value = aiOutput, onValueChange = { aiOutput = it }, label = { Text("AI JSON output") }, modifier = Modifier.fillMaxWidth().height(140.dp))
                Button(onClick = { viewModel.importAiJson(aiOutput) }) { Text("Import generated JSON") }

                Divider()
                Text("Developer: Shishir", fontWeight = FontWeight.SemiBold)
                Text("GitHub: https://github.com/Shishir-ip")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )

    LaunchedEffect(state.categories.size) {
        selectedExport = selectedExport ?: state.categories.firstOrNull()
    }
}
