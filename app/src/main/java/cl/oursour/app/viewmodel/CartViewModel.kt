package cl.oursour.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.AppContainer
import cl.oursour.app.data.local.OrderItemEntity
import cl.oursour.app.data.local.OrderWithTotal
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartUiState(val items: List<OrderItemEntity> = emptyList()) {
    val total: Int get() = items.sumOf { it.quantity * it.unitPrice }
    val count: Int get() = items.sumOf { it.quantity }
}

sealed interface CartEvent {
    data class Message(val text: String) : CartEvent
    data class Ordered(val orderId: Long) : CartEvent
}

/** CU-04: carro de compras (cambiar cantidad, eliminar y solicitar el pedido simulado). */
class CartViewModel(container: AppContainer) : ViewModel() {

    private val repo = container.cartRepository
    private val userId = container.session.current?.userId ?: -1L

    val uiState: StateFlow<CartUiState> = repo.observeCart(userId)
        .map { CartUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    private val _events = MutableSharedFlow<CartEvent>(extraBufferCapacity = 4)
    val events: SharedFlow<CartEvent> = _events.asSharedFlow()

    fun changeQuantity(item: OrderItemEntity, delta: Int) {
        viewModelScope.launch { repo.changeQuantity(item, delta) }
    }

    fun remove(item: OrderItemEntity) {
        viewModelScope.launch { repo.remove(item) }
    }

    fun checkout() {
        viewModelScope.launch {
            val result = repo.checkout(userId)
            if (result.isSuccess) {
                _events.tryEmit(CartEvent.Ordered(result.getOrThrow()))
            } else {
                _events.tryEmit(CartEvent.Message(result.exceptionOrNull()?.message ?: "No se pudo solicitar"))
            }
        }
    }
}

/** Un pedido ya solicitado junto con sus líneas de detalle. */
data class OrderUi(val order: OrderWithTotal, val items: List<OrderItemEntity>)

/** Historial de pedidos solicitados. */
class OrdersViewModel(container: AppContainer) : ViewModel() {

    private val repo = container.cartRepository
    private val userId = container.session.current?.userId ?: -1L

    val orders: StateFlow<List<OrderUi>> = combine(
        repo.observeHistory(userId),
        repo.observeHistoryItems(userId)
    ) { orders, items ->
        orders.map { order -> OrderUi(order, items.filter { it.orderId == order.id }) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
