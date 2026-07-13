package com.shishir.routineplannerpro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.shishir.routineplannerpro.AppContainer
import com.shishir.routineplannerpro.data.RoutineRepository
import com.shishir.routineplannerpro.model.AiRoutineOutput
import com.shishir.routineplannerpro.model.ExportRoutine
import com.shishir.routineplannerpro.model.RoutineCategoryEntity
import com.shishir.routineplannerpro.model.RoutineItemEntity
import com.shishir.routineplannerpro.network.OpenRouterService
import com.shishir.routineplannerpro.reminder.ReminderScheduler
import com.shishir.routineplannerpro.settings.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AppViewModel(
    private val repository: RoutineRepository,
    private val settingsRepository: SettingsRepository,
    private val openRouterService: OpenRouterService,
    private val scheduler: ReminderScheduler
) : ViewModel() {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private val selectedCategoryId = MutableStateFlow<Long?>(null)
    private val selectedDate = MutableStateFlow(java.time.LocalDate.now())
    private val toastMessage = MutableStateFlow<String?>(null)

    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val items = repository.allItems.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val darkTheme = settingsRepository.isDarkTheme.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val apiKey = settingsRepository.apiKey.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val uiState: StateFlow<AppUiState> = combine(
        categories,
        items,
        selectedCategoryId,
        selectedDate,
        toastMessage
    ) { categories, items, selectedCategoryId, selectedDate, toast ->
        val activeCategoryId = selectedCategoryId ?: categories.firstOrNull()?.id
        AppUiState(
            categories = categories,
            allItems = items,
            selectedCategoryId = activeCategoryId,
            selectedDate = selectedDate,
            toastMessage = toast
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppUiState())

    init {
        viewModelScope.launch {
            repository.ensureSystemCategories()
        }
    }

    fun setMessage(message: String?) {
        toastMessage.value = message
    }

    fun selectCategory(id: Long) {
        selectedCategoryId.value = id
    }

    fun setSelectedDate(date: java.time.LocalDate) {
        selectedDate.value = date
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.addCategory(name)
            selectedCategoryId.value = id
            toastMessage.value = "$name added"
        }
    }

    fun deleteCategory(category: RoutineCategoryEntity) {
        viewModelScope.launch {
            repository.removeCategory(category)
            toastMessage.value = "${category.name} deleted"
        }
    }

    fun addItem(item: RoutineItemEntity) {
        viewModelScope.launch {
            val id = repository.addItem(item)
            scheduler.schedule(item.copy(id = id))
            toastMessage.value = "Activity added"
        }
    }

    fun deleteItem(item: RoutineItemEntity) {
        viewModelScope.launch {
            repository.deleteItem(item)
            toastMessage.value = "Activity deleted"
        }
    }

    fun toggleTheme(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(enabled)
        }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            settingsRepository.saveApiKey(key)
            toastMessage.value = "API key saved"
        }
    }

    fun deleteApiKey() {
        viewModelScope.launch {
            settingsRepository.deleteApiKey()
            toastMessage.value = "API key removed"
        }
    }

    fun maskedApiKey(): String = settingsRepository.maskKey(apiKey.value)

    suspend fun exportRoutine(category: RoutineCategoryEntity): String {
        val payload = repository.exportCategory(category)
        return json.encodeToString(ExportRoutine.serializer(), payload)
    }

    fun importRoutineJson(raw: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val payload = json.decodeFromString(ExportRoutine.serializer(), raw)
                repository.importRoutine(payload)
            }.onSuccess {
                toastMessage.value = "Routine imported"
            }.onFailure {
                toastMessage.value = "Import failed: ${it.message}"
            }
        }
    }

    fun importAiJson(raw: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val payload = json.decodeFromString(AiRoutineOutput.serializer(), raw)
                payload.routines.forEach { repository.importAiBlock(it) }
            }.onSuccess {
                toastMessage.value = "AI routine imported"
            }.onFailure {
                toastMessage.value = "AI import failed: ${it.message}"
            }
        }
    }

    fun generateFromAi(prompt: String, onResult: (String?, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            openRouterService.generateJson(apiKey.value, prompt)
                .onSuccess {
                    withContext(Dispatchers.Main) { onResult(it, null) }
                }
                .onFailure {
                    withContext(Dispatchers.Main) { onResult(null, it.message ?: "Unknown error") }
                }
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AppViewModel(
                        repository = container.repository,
                        settingsRepository = container.settingsRepository,
                        openRouterService = container.openRouterService,
                        scheduler = container.scheduler
                    ) as T
                }
            }
        }
    }
}

data class AppUiState(
    val categories: List<RoutineCategoryEntity> = emptyList(),
    val allItems: List<RoutineItemEntity> = emptyList(),
    val selectedCategoryId: Long? = null,
    val selectedDate: java.time.LocalDate = java.time.LocalDate.now(),
    val toastMessage: String? = null
)
