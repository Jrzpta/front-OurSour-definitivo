package cl.oursour.app.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.data.local.ProductEntity
import cl.oursour.app.data.local.TrendRow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------------
// CU-05: gestión del catálogo
// ---------------------------------------------------------------------------

class AdminCatalogViewModel(container: AppContainer) : ViewModel() {

    private val repo = container.catalogRepository

    val products: StateFlow<List<ProductEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setActive(product: ProductEntity, active: Boolean) {
        viewModelScope.launch { repo.setActive(product, active) }
    }
}

data class ProductFormState(
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val stock: String = "",
    val active: Boolean = true,
    val emoji: String = "🍹",
    val nameError: String? = null,
    val priceError: String? = null,
    val stockError: String? = null,
    val isEditing: Boolean = false
)

class AdminProductFormViewModel(
    savedStateHandle: SavedStateHandle,
    container: AppContainer
) : ViewModel() {

    private val repo = container.catalogRepository
    private val productId: Long = savedStateHandle["productId"] ?: -1L
    private var original: ProductEntity? = null

    private val _state = MutableStateFlow(ProductFormState())
    val state: StateFlow<ProductFormState> = _state.asStateFlow()

    init {
        if (productId > 0) {
            viewModelScope.launch {
                repo.findById(productId)?.let { p ->
                    original = p
                    _state.value = ProductFormState(
                        name = p.name,
                        description = p.description,
                        price = p.price.toString(),
                        stock = p.stock.toString(),
                        active = p.active,
                        emoji = p.emoji,
                        isEditing = true
                    )
                }
            }
        }
    }

    fun onName(value: String) { _state.value = _state.value.copy(name = value, nameError = null) }
    fun onDescription(value: String) { _state.value = _state.value.copy(description = value) }
    fun onPrice(value: String) { _state.value = _state.value.copy(price = value, priceError = null) }
    fun onStock(value: String) { _state.value = _state.value.copy(stock = value, stockError = null) }
    fun onActive(value: Boolean) { _state.value = _state.value.copy(active = value) }
    fun onEmoji(value: String) { _state.value = _state.value.copy(emoji = value) }

    /** Valida los datos (flujo alternativo de CU-05) y, si están bien, guarda en el catálogo. */
    fun save(onSaved: () -> Unit) {
        val s = _state.value
        val price = s.price.trim().toIntOrNull()
        val stock = s.stock.trim().toIntOrNull()

        val nameError = if (s.name.isBlank()) "Ingresa el nombre comercial" else null
        val priceError = when {
            price == null -> "Ingresa un precio válido (solo números)"
            price <= 0 -> "El precio no puede ser negativo ni cero"
            else -> null
        }
        val stockError = when {
            stock == null -> "Ingresa el stock (solo números)"
            stock < 0 -> "El stock no puede ser negativo"
            else -> null
        }

        if (nameError != null || priceError != null || stockError != null) {
            _state.value = s.copy(nameError = nameError, priceError = priceError, stockError = stockError)
            return
        }

        viewModelScope.launch {
            val base = original ?: ProductEntity(name = "", description = "", price = 0, stock = 0)
            repo.save(
                base.copy(
                    name = s.name.trim(),
                    description = s.description.trim(),
                    price = price!!,
                    stock = stock!!,
                    active = s.active,
                    emoji = s.emoji
                )
            )
            onSaved()
        }
    }
}

// ---------------------------------------------------------------------------
// CU-06: métricas y tendencias
// ---------------------------------------------------------------------------

enum class MetricsRange(val label: String, val days: Int) {
    WEEK("7 días", 7),
    MONTH("30 días", 30),
    ALL("Todo", 0)
}

data class MetricsUiState(
    val range: MetricsRange = MetricsRange.ALL,
    val totalSearches: Int = 0,
    val topIngredients: List<TrendRow> = emptyList(),
    val topRecipes: List<TrendRow> = emptyList()
)

@OptIn(ExperimentalCoroutinesApi::class)
class AdminMetricsViewModel(container: AppContainer) : ViewModel() {

    private val repo = container.metricsRepository
    private val range = MutableStateFlow(MetricsRange.ALL)

    val uiState: StateFlow<MetricsUiState> = range
        .flatMapLatest { selected ->
            val since = if (selected.days == 0) 0L
            else System.currentTimeMillis() - selected.days * 24L * 3_600_000L
            combine(
                repo.observeTotal(since),
                repo.observeTopIngredients(since),
                repo.observeTopRecipes(since)
            ) { total, ingredients, recipes ->
                MetricsUiState(selected, total, ingredients, recipes)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MetricsUiState())

    fun setRange(newRange: MetricsRange) {
        range.value = newRange
    }
}
