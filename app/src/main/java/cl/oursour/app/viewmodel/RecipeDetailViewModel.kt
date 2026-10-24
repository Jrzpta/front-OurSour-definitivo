package cl.oursour.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.data.repository.RecipeMatch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DetailState {
    data object Loading : DetailState
    data class Success(val match: RecipeMatch) : DetailState
    data class Error(val message: String) : DetailState
}

/** CU-03 y CU-04: detalle de la receta y botón "Agregar al carro". */
@OptIn(ExperimentalCoroutinesApi::class)
class RecipeDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val container: AppContainer
) : ViewModel() {

    private val recipeId: Long = savedStateHandle["recipeId"] ?: -1L
    private val userId = container.session.current?.userId ?: -1L
    private val recipeRepo = container.recipeRepository

    private val reload = MutableStateFlow(0)

    private val _messages = MutableSharedFlow<UiMessage>(extraBufferCapacity = 4)
    val messages: SharedFlow<UiMessage> = _messages.asSharedFlow()

    val state: StateFlow<DetailState> = reload
        .flatMapLatest {
            recipeRepo.observeMatch(userId, recipeId)
                .map<RecipeMatch?, DetailState> { match ->
                    if (match == null) DetailState.Error("No pudimos cargar esta receta.")
                    else DetailState.Success(match)
                }
                .onStart { emit(DetailState.Loading) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailState.Loading)

    init {
        // Registramos la consulta de la receta para las métricas (CU-06).
        viewModelScope.launch {
            recipeRepo.getRecipe(recipeId)?.let { recipeRepo.logRecipeView(it.title) }
        }
    }

    /** Flujo alternativo de CU-03: reintentar la carga si algo falló. */
    fun retry() {
        reload.value = reload.value + 1
    }

    /** CU-04: agrega el producto Our Sour asociado al carro (cantidad 1). */
    fun addToCart() {
        val product = (state.value as? DetailState.Success)?.match?.product ?: return
        viewModelScope.launch {
            val result = container.cartRepository.addProduct(userId, product)
            if (result.isSuccess) {
                _messages.tryEmit(UiMessage("${product.name} agregado al carro", "Ver carro"))
            } else {
                _messages.tryEmit(UiMessage(result.exceptionOrNull()?.message ?: "No se pudo agregar"))
            }
        }
    }
}
