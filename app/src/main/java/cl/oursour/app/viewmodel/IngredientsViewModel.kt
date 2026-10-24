package cl.oursour.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.data.local.Categories
import cl.oursour.app.data.local.IngredientWithOwned
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class IngredientsUiState(
    /** Ingredientes agrupados por categoría (ya filtrados por el buscador). */
    val groups: List<Pair<String, List<IngredientWithOwned>>> = emptyList(),
    val selected: Set<Long> = emptySet(),
    val hasChanges: Boolean = false,
    val query: String = ""
)

/** CU-01: declarar ingredientes disponibles. */
class IngredientsViewModel(container: AppContainer) : ViewModel() {

    private val repo = container.ingredientRepository
    private val userId = container.session.current?.userId ?: -1L

    private val selected = MutableStateFlow<Set<Long>>(emptySet())
    private val saved = MutableStateFlow<Set<Long>>(emptySet())
    private val query = MutableStateFlow("")

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    val uiState: StateFlow<IngredientsUiState> = combine(
        repo.observe(userId), selected, saved, query
    ) { items, sel, savedSet, q ->
        val filtered = if (q.isBlank()) items else items.filter { it.name.contains(q.trim(), ignoreCase = true) }
        val groups = Categories.all.mapNotNull { category ->
            val list = filtered.filter { it.category == category }
            if (list.isEmpty()) null else category to list
        }
        IngredientsUiState(groups = groups, selected = sel, hasChanges = sel != savedSet, query = q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IngredientsUiState())

    init {
        // Carga la selección guardada en Room una sola vez.
        viewModelScope.launch {
            val owned = repo.getOwnedIds(userId).toSet()
            selected.value = owned
            saved.value = owned
        }
    }

    fun toggle(id: Long) {
        selected.value = if (id in selected.value) selected.value - id else selected.value + id
    }

    fun setQuery(text: String) {
        query.value = text
    }

    fun save() {
        viewModelScope.launch {
            val toSave = selected.value
            repo.saveSelection(userId, toSave)
            saved.value = toSave
            val text = if (toSave.isEmpty()) "Guardaste tu lista vacía" else "¡Listo! Guardaste ${toSave.size} ingredientes"
            _messages.tryEmit(UiMessage(text, if (toSave.isEmpty()) null else "Ver recetas"))
        }
    }

    /** Flujo alternativo de CU-01: ingrediente que no está en el listado. */
    fun addCustom(name: String, category: String) {
        viewModelScope.launch {
            val result = repo.addCustom(name, category)
            if (result.isSuccess) {
                val newId = result.getOrThrow()
                selected.value = selected.value + newId
                _messages.tryEmit(UiMessage("\"${name.trim()}\" agregado. Recuerda guardar tu lista"))
            } else {
                _messages.tryEmit(UiMessage(result.exceptionOrNull()?.message ?: "No se pudo agregar"))
            }
        }
    }
}
