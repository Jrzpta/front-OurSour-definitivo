package cl.oursour.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.data.repository.RecipeMatch
import cl.oursour.app.data.repository.rankedByRelevance
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Pantalla de inicio: saludo, cantidad de ingredientes y recetas destacadas. */
class HomeViewModel(container: AppContainer) : ViewModel() {

    private val session = container.session.current
    private val userId = session?.userId ?: -1L

    val userName: String = session?.name.orEmpty()

    val ownedCount: StateFlow<Int> = container.ingredientRepository
        .observeOwnedIds(userId)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    val featured: StateFlow<List<RecipeMatch>> = container.recipeRepository
        .observeMatches(userId)
        .map { it.rankedByRelevance().take(8) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
