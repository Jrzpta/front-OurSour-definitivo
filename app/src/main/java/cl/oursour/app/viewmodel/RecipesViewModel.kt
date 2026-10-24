package cl.oursour.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.data.local.ProductEntity
import cl.oursour.app.data.repository.RecipeMatch
import cl.oursour.app.data.repository.rankedByRelevance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class RecipeFilter(val label: String) {
    ALL("Todas"),
    COCKTAIL("Cócteles"),
    MOCKTAIL("Mocktails")
}

data class RecipesUiState(
    val loading: Boolean = true,
    val ownedCount: Int = 0,
    val results: List<RecipeMatch> = emptyList(),
    val suggestedProducts: List<ProductEntity> = emptyList(),
    val filter: RecipeFilter = RecipeFilter.ALL,
    val onlyCompatible: Boolean = true
)

/** CU-02: buscar recetas compatibles con los ingredientes declarados. */
class RecipesViewModel(container: AppContainer) : ViewModel() {

    private val recipeRepo = container.recipeRepository
    private val ingredientRepo = container.ingredientRepository
    private val userId = container.session.current?.userId ?: -1L

    private val filter = MutableStateFlow(RecipeFilter.ALL)
    private val onlyCompatible = MutableStateFlow(true)
    private var lastLoggedKey = ""

    val uiState: StateFlow<RecipesUiState> = combine(
        recipeRepo.observeMatches(userId),
        ingredientRepo.observeOwnedIds(userId),
        filter,
        onlyCompatible
    ) { matches, ownedIds, currentFilter, only ->
        val byType = matches.filter {
            when (currentFilter) {
                RecipeFilter.ALL -> true
                RecipeFilter.COCKTAIL -> it.recipe.alcoholic
                RecipeFilter.MOCKTAIL -> !it.recipe.alcoholic
            }
        }
        val ranked = byType.rankedByRelevance()
        val results = if (only) ranked.filter { it.percent > 0 } else ranked

        // Flujo alternativo de CU-02: sin coincidencias -> sugerir productos Our Sour.
        val suggestions = if (results.isEmpty()) {
            matches.rankedByRelevance()
                .mapNotNull { it.product }
                .filter { it.active }
                .distinctBy { it.id }
                .take(3)
        } else {
            emptyList()
        }
        RecipesUiState(
            loading = false,
            ownedCount = ownedIds.size,
            results = results,
            suggestedProducts = suggestions,
            filter = currentFilter,
            onlyCompatible = only
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipesUiState())

    fun setFilter(newFilter: RecipeFilter) {
        filter.value = newFilter
    }

    fun setOnlyCompatible(value: Boolean) {
        onlyCompatible.value = value
    }

    /**
     * Registra la búsqueda (ingredientes usados) para las métricas del administrador.
     * Solo registra si la lista de ingredientes cambió desde la última vez.
     */
    fun logSearch() {
        viewModelScope.launch {
            val names = ingredientRepo.getOwnedNames(userId)
            val key = names.joinToString("|")
            if (names.isNotEmpty() && key != lastLoggedKey) {
                recipeRepo.logSearch(names)
                lastLoggedKey = key
            }
        }
    }
}
